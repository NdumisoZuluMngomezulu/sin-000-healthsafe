package co.wethinkcode.healthsafe.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.wethinkcode.healthsafe.mq.MqConfig;
import io.javalin.http.Context;

/**
 * Consumer-side handling for equipment failure alerts delivered over the
 * guaranteed-delivery equipment-failure-queue.
 */
public class EquipmentAlertHandler {

    public static final List<Map<String, Object>> receivedAlerts = new CopyOnWriteArrayList<>();
    private static final ObjectMapper mapper = MqConfig.mapper;

    private EquipmentAlertHandler() {
    }

    @SuppressWarnings("unchecked")
    public static void handle(String json) {
        try {
            Map<String, Object> alert = mapper.readValue(json, Map.class);
            receivedAlerts.add(alert);
            System.out.println("equipment-alert-service: received failure alert for equipment '"
                    + alert.get("equipment") + "' (qty " + alert.get("quantity") + ") on ward "
                    + alert.get("wardId") + " [" + alert.get("department") + "]");
        } catch (Exception e) {
            System.out.println("Exception " + e.getMessage());
        }
       
        
    }

    public static void listAlerts(Context ctx) {
        ctx.json(receivedAlerts);
    }
}
