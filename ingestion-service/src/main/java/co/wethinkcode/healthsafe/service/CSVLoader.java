package co.wethinkcode.healthsafe.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.wethinkcode.healthsafe.model.Ward;

/**
 * Reads and cleans the messy legacy {@code wards-outdated.csv} export.
 *
 * Handles (see ingestion-service/README.md "Known data issues"):
 *  - inconsistent casing in IDs, wings, departments
 *  - padding: leading/trailing spaces and double-spaces inside fields
 *  - duplicate records for the same real ward (different ID casing/values) -
 *    merged, preferring whichever record has the more complete/valid data
 *  - missing / placeholder bed counts (blank, N/A, TBD, unknown, -, NaN)
 *  - invalid / non-numeric bed counts (negative, spelled-out, unrealistic)
 *  - naming/spelling variants for the same department (e.g. Pediatrics vs
 *    Paediatrics)
 */
public class CSVLoader {

    private static final String FILE_PATH = "/wards-outdated.csv";

    private static final Set<String> MISSING_TOKENS = Set.of(
            "", "n/a", "na", "tbd", "unknown", "-", "nan"
    );

    /** Canonicalises department spelling/casing variants to one name. */
    private static final Map<String, String> DEPARTMENT_CANONICAL = Map.ofEntries(
            Map.entry("cardiology", "Cardiology"),
            Map.entry("paediatrics", "Paediatrics"),
            Map.entry("pediatrics", "Paediatrics"), // regional spelling variant
            Map.entry("oncology", "Oncology"),
            Map.entry("radiology", "Radiology"),
            Map.entry("maternity", "Maternity"),
            Map.entry("icu", "ICU")
    );

    // A ward realistically won't have more beds than this - values above are
    // treated as unrealistic/likely data-entry errors (e.g. a year typed in
    // by mistake).
    private static final int MAX_PLAUSIBLE_BEDS = 100;

    public List<Ward> getWards() throws IOException {
        Map<String, Ward> byId = new LinkedHashMap<>();

        try (InputStream inputStream = CSVLoader.class.getResourceAsStream(FILE_PATH)) {
            if (inputStream == null) {
                throw new IOException("Could not find resource: " + FILE_PATH);
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                String line;
                boolean first = true;
                while ((line = reader.readLine()) != null) {
                    if (first) {
                        // header row - skip
                        first = false;
                        continue;
                    }
                    if (line.isBlank()) {
                        continue;
                    }
                    Ward ward = parseRow(line);
                    mergeIntoMap(byId, ward);
                }
            }
        }

        return new ArrayList<>(byId.values());
    }

    private void mergeIntoMap(Map<String, Ward> byId, Ward incoming) {
        Ward existing = byId.get(incoming.getWardId());
        if (existing == null) {
            byId.put(incoming.getWardId(), incoming);
            return;
        }

        // Duplicate real-world ward (same normalised ID, different
        // casing/values in the source data). Merge: keep whichever value is
        // present for each field, preferring the existing record but filling
        // gaps from the incoming one, and note that a merge happened.
        String wing = existing.getWing() != null ? existing.getWing() : incoming.getWing();
        String department = existing.getDepartment() != null ? existing.getDepartment() : incoming.getDepartment();
        Integer beds = existing.getBedsAvailable() != null ? existing.getBedsAvailable() : incoming.getBedsAvailable();

        StringBuilder notes = new StringBuilder("merged duplicate record for " + incoming.getWardId());
        if (existing.getNotes() != null) notes.append("; ").append(existing.getNotes());
        if (incoming.getNotes() != null) notes.append("; ").append(incoming.getNotes());

        byId.put(incoming.getWardId(), new Ward(incoming.getWardId(), wing, department, beds, notes.toString()));
    }

    private Ward parseRow(String line) {
        String[] values = line.split(",", -1);
        String rawId = values.length > 0 ? values[0] : "";
        String rawWing = values.length > 1 ? values[1] : "";
        String rawDept = values.length > 2 ? values[2] : "";
        String rawBeds = values.length > 3 ? values[3] : "";

        String wardId = cleanId(rawId);
        String wing = cleanWing(rawWing);
        String department = cleanDepartment(rawDept);

        List<String> notes = new ArrayList<>();
        if (wing == null) notes.add("wing missing");
        if (department == null) notes.add("department not recognised ('" + collapse(rawDept) + "')");

        Integer beds = cleanBeds(rawBeds, notes);

        String notesText = notes.isEmpty() ? null : String.join("; ", notes);
        return new Ward(wardId, wing, department, beds, notesText);
    }

    private String cleanId(String raw) {
        return collapse(raw).toUpperCase();
    }

    private String cleanWing(String raw) {
        String value = collapse(raw);
        if (value.isEmpty()) {
            return null;
        }
        return titleCase(value);
    }

    private String cleanDepartment(String raw) {
        String value = collapse(raw).toLowerCase();
        return DEPARTMENT_CANONICAL.get(value);
    }

    private Integer cleanBeds(String raw, List<String> notes) {
        String value = collapse(raw);

        if (MISSING_TOKENS.contains(value.toLowerCase())) {
            notes.add("bedsAvailable was missing/placeholder ('" + raw.trim() + "') - flagged for follow-up");
            return null;
        }

        int parsed;
        try {
            parsed = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            notes.add("bedsAvailable was non-numeric ('" + value + "') - flagged for follow-up");
            return null;
        }

        if (parsed < 0) {
            notes.add("bedsAvailable was negative (" + parsed + ") - flagged for follow-up");
            return null;
        }

        if (parsed > MAX_PLAUSIBLE_BEDS) {
            notes.add("bedsAvailable was unrealistic (" + parsed + ") - flagged for follow-up");
            return null;
        }

        return parsed;
    }

    /** Trims and collapses any run of internal whitespace to a single space. */
    private String collapse(String raw) {
        if (raw == null) return "";
        return raw.trim().replaceAll("\\s+", " ");
    }

    private String titleCase(String value) {
        String[] words = value.toLowerCase().split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (words[i].isEmpty()) continue;
            if (sb.length() > 0) sb.append(" ");
            sb.append(Character.toUpperCase(words[i].charAt(0))).append(words[i].substring(1));
        }
        return sb.toString();
    }
}
