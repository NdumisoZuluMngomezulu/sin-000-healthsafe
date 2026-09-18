package co.wethinkcode.healthsafe.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.javalin.http.Context;

import co.wethinkcode.healthsafe.model.Equipment;
import co.wethinkcode.healthsafe.model.Ward;
import co.wethinkcode.healthsafe.mq.MqConfig;

public class WardServiceHandler {

    // wardId -> Ward, populated from ingestion-service on startup.
    public static final Map<String, Ward> wards = new ConcurrentHashMap<>();

    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String ingestionApiUrl = "http://localhost:7030";

    private WardServiceHandler() {
    }

    /** Fetches the cleaned ward list from ingestion-service and caches it. */
    public static void loadWardsFromIngestion() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ingestionApiUrl + "/wards"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            List<Ward> loaded = objectMapper.readValue(response.body(), new TypeReference<List<Ward>>() {});

            wards.clear();
            for (Ward ward : loaded) {
                wards.put(ward.getWardId(), ward);
            }
            System.out.println("ward-service: loaded " + wards.size() + " wards from ingestion-service");
        } catch (Exception e) {
            System.out.println("ward-service: could not reach ingestion-service (" + e.getMessage()
                    + ") - will retry lazily on next request");
        }
    }

    public static void getWards(Context ctx) {
        if (wards.isEmpty()) {
            loadWardsFromIngestion();
        }
        ctx.json(new ArrayList<>(wards.values()));
    }

    public static void getWardById(Context ctx) {
        if (wards.isEmpty()) {
            loadWardsFromIngestion();
        }
        String id = ctx.pathParam("id").toUpperCase();
        Ward ward = wards.get(id);
        if (ward == null) {
            ctx.status(404).json(Map.of("error", "unknown ward " + id));
            return;
        }
        ctx.json(ward);
    }

    public static void departments(Context ctx) {
        if (wards.isEmpty()) {
            loadWardsFromIngestion();
        }
        TreeSet<String> departments = new TreeSet<>();
        for (Ward ward : wards.values()) {
            if (ward.getDepartment() != null) {
                departments.add(ward.getDepartment());
            }
        }
        ctx.json(new ArrayList<>(departments));
    }

    /**
     * Records/updates a piece of equipment on a ward. Body: {"name": "...",
     * "quantity": N, "damaged": true|false}. When a piece of equipment is
     * newly reported damaged, publishes a guaranteed-delivery alert to the
     * equipment-failure-queue for equipment-alert-service (stage 4).
     */
    public static void reportEquipment(Context ctx) {
        String id = ctx.pathParam("id").toUpperCase();
        Ward ward = wards.get(id);
        if (ward == null) {
            ctx.status(404).json(Map.of("error", "unknown ward " + id));
            return;
        }

        Equipment equipment = ctx.bodyAsClass(Equipment.class);
        ward.addEquipment(equipment);

        if (equipment.isDamaged()) {
            publishEquipmentFailure(ward, equipment);
        }

        ctx.status(200).json(Map.of("status", "equipment recorded", "faulty", ward.faultyEquipment()));
    }

    public static void getEquipment(Context ctx) {
        String id = ctx.pathParam("id").toUpperCase();
        Ward ward = wards.get(id);
        if (ward == null) {
            ctx.status(404).json(Map.of("error", "unknown ward " + id));
            return;
        }
        ctx.json(ward.getEquipmentList());
    }

    private static void publishEquipmentFailure(Ward ward, Equipment equipment) {
        try (Connection connection = MqConfig.createConnection()) {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(MqConfig.QUEUE);
            MessageProducer producer = session.createProducer(destination);
            // Guaranteed delivery: persist the message so it survives a broker restart.
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);

            Map<String, Object> alert = new LinkedHashMap<>();
            alert.put("wardId", ward.getWardId());
            alert.put("department", ward.getDepartment());
            alert.put("equipment", equipment.getName());
            alert.put("quantity", equipment.getQuantity());

            String jsonPayload = MqConfig.mapper.writeValueAsString(alert);
            TextMessage message = session.createTextMessage(jsonPayload);
            producer.send(message);

            System.out.println("ward-service: published equipment failure for " + equipment.getName()
                    + " on ward " + ward.getWardId() + " to " + MqConfig.QUEUE);
        } catch (Exception e) {
            System.err.println("ward-service: failed to publish equipment failure - " + e.getMessage());
        }
    }

    /**
     * Applies a schedule/status change broadcast on staffing-events-topic
     * (stage 3) to this service's in-memory ward record, so ward-service
     * stays in sync without polling staffing-service.
     */
    @SuppressWarnings("unchecked")
    public static void applyStaffingEvent(String json) {
        try {
            Map<String, Object> event = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
            Object payloadObj = event.get("payload");
            if (!(payloadObj instanceof Map)) {
                return;
            }
            Map<String, Object> payload = (Map<String, Object>) payloadObj;

            String wardId = String.valueOf(payload.get("wardId"));
            Ward ward = wards.get(wardId);
            if (ward == null) {
                System.out.println("ward-service: staffing event for unknown ward " + wardId + " - ignoring");
                return;
            }

            Object alertLevelObj = payload.get("alertLevel");
            if (alertLevelObj instanceof Number) {
                ward.setAlertLevel(((Number) alertLevelObj).intValue());
            }

            Object doctorsObj = payload.get("assignedDoctors");
            if (doctorsObj instanceof List) {
                List<String> names = new ArrayList<>();
                for (Object d : (List<Object>) doctorsObj) {
                    if (d instanceof Map) {
                        Object name = ((Map<String, Object>) d).get("name");
                        names.add(name != null ? String.valueOf(name) : String.valueOf(d));
                    } else {
                        names.add(String.valueOf(d));
                    }
                }
                ward.setAssignedDoctors(names);
            }

            System.out.println("ward-service: applied staffing event ("
                    + event.get("eventType") + ") for ward " + wardId);
        } catch (Exception e) {
            System.err.println("ward-service: could not process staffing event - " + e.getMessage());
        }
    }
}
