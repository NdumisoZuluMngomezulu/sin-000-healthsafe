package co.wethinkcode.healthsafe.service;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import io.javalin.http.Context;

import co.wethinkcode.healthsafe.model.Equipment;
import co.wethinkcode.healthsafe.model.Ward;
import co.wethinkcode.healthsafe.mq.MqConfig;

public class WardServiceHandler {
    public static List<Ward> wards = new ArrayList<>();
    public static HttpClient client = HttpClient.newHttpClient();
    public static ObjectMapper objectMapper = new ObjectMapper();
    public static String ingestionApiUrl = "http://localhost:7030";

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

    public static void publishToWardQueue(Equipment equipment){
        try (Connection connection = MqConfig.createConnection();
              Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
            
            Destination destination = session.createQueue("ward-service-queue");
            MessageProducer producer = session.createProducer(destination);

            String jsonPayload = MqConfig.mapper.writeValueAsString(equipment);
            TextMessage message = session.createTextMessage(jsonPayload);

            producer.send(message);
            System.out.println("MQ has sent event " + equipment.getName() + " details to Equipment service");

            
        } catch (Exception e) {
            System.err.println("MQ failed to send equipment information");
        }
    }

    public static void processSchedule(TextMessage message){
        try {
            String jsonText = message.getText();

            Map<String, Object> scheduleMap = objectMapper.readValue(jsonText, new TypeReference<Map<String, Object>>(){});
            int alertLevel = (int) scheduleMap.get("alertLevel");

            Map<String, Object> wardMap = (Map<String, Object>) scheduleMap.get("ward");
            String department = (String) scheduleMap.get("department");
            List<Map<String, Object>> assignedDoctors = (List<Map<String, Object>>) wardMap.get("assignedDoctors");
            String wing = (String) wardMap.get("wing");
            String id = (String) wardMap.get("id");

            Ward ward = new Ward(id, wing, department);
            ward.setDoctors(assignedDoctors);
            ward.setAlert(alertLevel);
            WardServiceHandler.wards.add(ward);

        } catch (Exception e) {
            System.err.println("Could not process message");
        }
    }

    public boolean checkFaultyEquipment(){
        
    }
}
/*
// 1. Convert the raw JSON string directly into a generic map
Map<String, Object> scheduleMap = mapper.readValue(jsonText, Map.class);

// 2. Extract top-level primitive values safely
String department = (String) scheduleMap.get("department");
int alertLevel = (int) scheduleMap.get("alertLevel");

// 3. Extract the nested 'ward' object fields
Map<String, Object> wardMap = (Map<String, Object>) scheduleMap.get("ward");
String wardId = null; 
if (wardMap != null) {
    // Adapt this field name to whatever your specific Ward model properties are named
    wardId = (String) wardMap.get("id"); 
}

// 4. Extract the nested list of 'assignedDoctors'
List<Map<String, Object>> doctorsList = (List<Map<String, Object>>) scheduleMap.get("assignedDoctors");

System.out.println("\n[Ward Service] --- New Schedule Event Interpreted ---");
System.out.println("Target Ward ID: " + wardId);
System.out.println("Department:     " + department);
System.out.println("Alert Level:    " + alertLevel);
System.out.println("Assigned Doctor Details:");

if (doctorsList != null) {
    for (Map<String, Object> doctor : doctorsList) {
        // Unpack individual fields from each doctor object block
        String doctorName = (String) doctor.get("name");
        String specialty = (String) doctor.get("specialty");
        System.out.println(" -> Doctor: " + doctorName + " (" + specialty + ")");
    }
} */
