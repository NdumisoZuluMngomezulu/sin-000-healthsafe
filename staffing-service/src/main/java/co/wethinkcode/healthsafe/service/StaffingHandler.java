package co.wethinkcode.healthsafe.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import io.javalin.http.Context;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.wethinkcode.healthsafe.mq.MqConfig;
import co.wethinkcode.healthsafe.model.Schedule;
import co.wethinkcode.healthsafe.model.StaffingEvent;
import co.wethinkcode.healthsafe.model.Ward;

public class StaffingHandler {

    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String wardServiceUrl = "http://localhost:7031";
    private static final String alertServiceUrl = "http://localhost:7032";

    // wardId -> most recently generated schedule.
    public static final Map<String, Schedule> wardSchedule = new ConcurrentHashMap<>();

    public StaffingHandler() {
    }

    /**
     * Generates (or refreshes) the on-call schedule for a ward:
     *  1. validate the ward via ward-service (404 passthrough)
     *  2. read the current Emergency Status via alert-level-service
     *  3. size the schedule to that status and broadcast it on
     *     staffing-events-topic (stage 3) instead of ward-service polling us.
     */
    public static void createSchedule(Context ctx) {
        String wardId = ctx.pathParam("wardId").toUpperCase();

        Ward ward = fetchWard(ctx, wardId);
        if (ward == null) {
            return; // fetchWard already wrote the error response
        }

        int alertLevel = fetchAlertLevel();

        if (DataLoader.doctors.isEmpty()) {
            try {
                DataLoader.loadDoctors();
            } catch (Exception e) {
                System.out.println("staffing-service: could not load doctors.csv - " + e.getMessage());
            }
        }

        Schedule schedule = new Schedule(ward, alertLevel, DataLoader.doctors);
        wardSchedule.put(wardId, schedule);

        StaffingEvent event = new StaffingEvent("SCHEDULE_UPDATED", schedule);
        publishToStaffingTopic(event);

        ctx.status(200).json(schedule);
    }

    public static void getSchedule(Context ctx) {
        String wardId = ctx.pathParam("wardId").toUpperCase();
        Schedule schedule = wardSchedule.get(wardId);
        if (schedule == null) {
            ctx.status(404).json(Map.of("error", "no schedule generated yet for ward " + wardId));
            return;
        }
        ctx.json(schedule);
    }

    /** Fetches a ward from ward-service, writing a matching error response on failure. */
    private static Ward fetchWard(Context ctx, String wardId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(wardServiceUrl + "/wards/" + wardId))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                ctx.status(404).json(Map.of("error", "unknown ward " + wardId));
                return null;
            }
            if (response.statusCode() != 200) {
                ctx.status(502).json(Map.of("error", "ward-service returned " + response.statusCode()));
                return null;
            }

            return objectMapper.readValue(response.body(), Ward.class);
        } catch (Exception e) {
            ctx.status(502).json(Map.of("error", "could not reach ward-service: " + e.getMessage()));
            return null;
        }
    }

    /** Reads the current Emergency Status; defaults to 1 (one on-call doctor) if unreachable. */
    private static int fetchAlertLevel() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(alertServiceUrl + "/alert-level"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Map<?, ?> body = objectMapper.readValue(response.body(), Map.class);
            Object level = body.get("level");
            if (level instanceof Number) {
                return ((Number) level).intValue();
            }
            return 1;
        } catch (Exception e) {
            System.out.println("staffing-service: could not reach alert-level-service ("
                    + e.getMessage() + ") - defaulting to alert level 1");
            return 1;
        }
    }

    private static void publishToStaffingTopic(StaffingEvent event) {
        try (Connection connection = MqConfig.createConnection()) {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Destination destination = session.createTopic(MqConfig.TOPIC);
            MessageProducer producer = session.createProducer(destination);

            String jsonPayload = MqConfig.mapper.writeValueAsString(event);
            TextMessage message = session.createTextMessage(jsonPayload);

            producer.send(message);
            System.out.println("staffing-service: published event " + event.getEventType()
                    + " to " + MqConfig.TOPIC);
        } catch (Exception e) {
            System.err.println("staffing-service: failed to publish staffing event - " + e.getMessage()
                    + ". Is the broker up? (`cd common && docker compose up -d`)");
        }
    }
}
