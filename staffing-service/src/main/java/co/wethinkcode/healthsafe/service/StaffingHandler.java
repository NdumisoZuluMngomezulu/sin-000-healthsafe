package co.wethinkcode.healthsafe.service;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import javax.jms.*;

import org.w3c.dom.Text;

import io.javalin.http.Context;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.wethinkcode.healthsafe.mq.MqConfig;
import co.wethinkcode.healthsafe.model.*;

public class StaffingHandler {
    public static HttpClient client = HttpClient.newHttpClient();
    public static int alertLevel;
    public static String ingestionApiUrl = "http://localhost:7030";
    public static String alertServiceUrl = "http://localhost:7032";
    public static ObjectMapper objectMapper = new ObjectMapper();
    public static Map<Ward, Schedule> ward_schedule = new HashMap<>();

    public StaffingHandler(){}

    public void getWards(Context ctx) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ingestionApiUrl + "/"))
                .GET()
                .build();
        
            HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString());
            
            List<Ward> wards = objectMapper.readValue(response.body(), new TypeReference<List<Ward>>() {});
        
            ctx.json(wards);

        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
        }
        
    }

    public static void getWardById(Context ctx) {
        String id = ctx.pathParam("id");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ingestionApiUrl +"/"+id))
                    .GET()
                    .build();
            
            HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString());
            
            Ward ward = objectMapper.readValue(response.body(), Ward.class);

            ctx.json(ward);
        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
        }
    }

    public void getAlertLevel() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(alertServiceUrl+"/alert-level"))
                    .GET()
                    .build();
            
            HttpResponse<String> response = client.send(
                        request, HttpResponse.BodyHandlers.ofString());
            
            StaffingHandler.alertLevel = objectMapper.readValue(response.body(), Integer.class);

        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
        }
    }

    public static void getSchedule(Context ctx) {
        Ward ward = ctx.bodyAsClass(Ward.class);

        Schedule schedule = new Schedule(ward);

        if (DataLoader.doctors.isEmpty()){
            try {
                DataLoader.loadDoctors();
            } catch (Exception e) {
                System.out.println("Error " + e.getMessage());
            }  
        }
        ward_schedule.put(ward, schedule);
        StaffingEvent event = new StaffingEvent("SCHEDULE CREATED", schedule);
        
        publishToStaffingQueue(event);

        ctx.status(200).json(Map.of("status","Schedule passed"));
    }

    public static void publishToStaffingQueue(StaffingEvent event) {
        try (Connection connection = MqConfig.createConnection()){
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Destination destination = session.createQueue("staffing-events-queue");
            MessageProducer producer = session.createProducer(destination);

            String jsonPayload = MqConfig.mapper.writeValueAsString(event);
            TextMessage message = session.createTextMessage(jsonPayload);

            producer.send(message);
            System.out.println("Message queue has sent event: " + event.getEventType() + " to staffing event queue");
        } catch (Exception e){
            System.err.println("MessageQueue failed to route queue");
        }
    }
}

// publishToStaffingQueue(event);

//         ctx.status(200).json(Map.of("status", "Cancellation notice broadcasted"));
//     }

//     // Unified helper method handling ActiveMQ transmission
//     private static void publishToStaffingQueue(StaffingEvent event) {
//         try (Connection connection = MqConfig.createConnection();
//              Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
            
//             // Both actions route directly through this single destination pipeline
//             Destination destination = session.createQueue("staffing.events.queue");
//             MessageProducer producer = session.createProducer(destination);

//             String jsonPayload = MqConfig.mapper.writeValueAsString(event);
//             TextMessage message = session.createTextMessage(jsonPayload);

//             producer.send(message);
//             System.out.println("[MQ] Sent event [" + event.getEventType() + "] to staffing.events.queue");

//         } catch (Exception e) {
//             System.err.println("[MQ Error] Failed to route staffing event: " + e.getMessage());
//         }
//     }
