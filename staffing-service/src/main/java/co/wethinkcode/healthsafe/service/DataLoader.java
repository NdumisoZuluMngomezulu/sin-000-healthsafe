package co.wethinkcode.healthsafe.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import co.wethinkcode.healthsafe.model.Doctor;

/**
 * Loads and cleans doctors.csv (doctor_id, wing, specialty, doctor name) -
 * same padding/casing/spelling issues as wards-outdated.csv, see
 * ingestion-service/README.md.
 */
public class DataLoader {

    private static final String FILE_PATH = "/doctors.csv";

    private static final Map<String, String> SPECIALTY_CANONICAL = Map.ofEntries(
            Map.entry("cardiology", "Cardiology"),
            Map.entry("paediatrics", "Paediatrics"),
            Map.entry("pediatrics", "Paediatrics"),
            Map.entry("oncology", "Oncology"),
            Map.entry("radiology", "Radiology"),
            Map.entry("maternity", "Maternity"),
            Map.entry("icu", "ICU")
    );

    public static final List<Doctor> doctors = new ArrayList<>();

    public static synchronized void loadDoctors() throws IOException {
        if (!doctors.isEmpty()) {
            return;
        }

        try (InputStream inputStream = DataLoader.class.getResourceAsStream(FILE_PATH)) {
            if (inputStream == null) {
                throw new IOException("Could not find resource: " + FILE_PATH);
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                String line;
                boolean first = true;
                while ((line = reader.readLine()) != null) {
                    if (first) {
                        first = false;
                        continue;
                    }
                    if (line.isBlank()) {
                        continue;
                    }
                    Doctor doctor = parseRow(line);
                    if (doctor != null) {
                        doctors.add(doctor);
                    }
                }
            }
        }
    }

    private static Doctor parseRow(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 4) {
            return null;
        }

        int id;
        try {
            id = Integer.parseInt(collapse(parts[0]));
        } catch (NumberFormatException e) {
            return null;
        }

        String specialty = SPECIALTY_CANONICAL.get(collapse(parts[2]).toLowerCase());
        String name = collapse(parts[3]);

        return new Doctor(id, name, specialty);
    }

    private static String collapse(String raw) {
        if (raw == null) return "";
        return raw.trim().replaceAll("\\s+", " ");
    }
}
