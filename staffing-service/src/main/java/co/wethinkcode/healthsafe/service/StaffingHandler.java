package co.wethinkcode.healthsafe.service;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import io.javalin.http.Context;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.wethinkcode.healthsafe.model.Ward;
import co.wethinkcode.healthsafe.model.Doctor;
import co.wethinkcode.healthsafe.model.Schedule;

public class StaffingHandler {
    public static HttpClient client = HttpClient.newHttpClient();
    public static int alertLevel;
    public static String ingestionApiUrl = "http://localhost:7030";
    public static String alertServiceUrl = "http://localhost:7032";
    public static ObjectMapper objectMapper = new ObjectMapper();
    public static Map<Ward, Schedule> ward_schedule = new HashMap<>();

    public StaffingHandler(){}

    public static void main(String[] args) {
        System.out.println("Hi");
    }

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

        ctx.json(schedule);

    }
}

// public static String fetchData(String apiBaseUrl, String path) {
//         try {
//             HttpRequest request = HttpRequest.newBuilder()
//                 .uri(URI.create(apiBaseUrl + path))
//                 .GET()
//                 .build();
            
//java.net.http.HttpResponse<String> response = httpClient.send(
// request, java.net.http.HttpResponse.BodyHandlers.ofString()); 
            
//             if (response.statusCode() == 200) { 
//                 String worldData = response.body();
//                 System.out.println("=====WORLD DATA=====");
//                 System.out.println(path + "' successfully restored via API."); 
//                 return worldData; 
//             } else { 
//                 System.out.println("API Error [" + response.statusCode() + "]: World configuration not found."); 
//                 return "error"; 
//             } 
//         } catch (Exception e) { 
//             System.out.println("Failed to forward restore command to API: " + e.getMessage()); 
//             // Crucial fix: return a fallback value or rethrow the exception here
//             return "error"; 
//         } 
//     }
