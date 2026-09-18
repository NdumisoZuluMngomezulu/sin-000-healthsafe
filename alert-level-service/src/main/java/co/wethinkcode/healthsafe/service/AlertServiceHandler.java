package co.wethinkcode.healthsafe.service;

import java.util.Map;

import io.javalin.http.Context;

/**
 * Tracks the hospital Emergency Status: an integer 0-8, where 8 is full Code
 * Blue. staffing-service reads this to size the on-call schedule.
 */
public class AlertServiceHandler {

    private static final int MIN_LEVEL = 0;
    private static final int MAX_LEVEL = 8;

    // Default to a calm baseline rather than 0, so a fresh on-call schedule
    // still gets at least one doctor per ward.
    public static volatile int alertLevel = 1;

    private AlertServiceHandler() {
    }

    public static void getAlertLevel(Context ctx) {
        ctx.json(Map.of("level", alertLevel));
    }

    public static void setAlertLevel(Context ctx) {
        Integer level = extractLevel(ctx);

        if (level == null || level < MIN_LEVEL || level > MAX_LEVEL) {
            ctx.status(400).json(Map.of(
                    "error", "level must be an integer between " + MIN_LEVEL + " and " + MAX_LEVEL
            ));
            return;
        }

        alertLevel = level;
        ctx.status(200).json(Map.of("status", "level updated", "level", alertLevel));
    }

    @SuppressWarnings("unchecked")
    private static Integer extractLevel(Context ctx) {
        // Accept either a JSON body {"level": N} or a numeric path param,
        // whichever the caller used.
        String pathLevel = ctx.pathParamMap().get("level");
        if (pathLevel != null) {
            return parseOrNull(pathLevel);
        }

        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            Object value = body.get("level");
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            if (value instanceof String) {
                return parseOrNull((String) value);
            }
        } catch (Exception e) {
            // fall through - no usable body
        }
        return null;
    }

    private static Integer parseOrNull(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
