package co.wethinkcode.healthsafe.service;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import io.javalin.http.Context;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.wethinkcode.healthsafe.model.Ward;

public class StaffingHandler {
    public static HttpClient client;
    public static String apiBaseUrl = "http://localHost:7030";
    public static ObjectMapper objectMapper = new ObjectMapper();
    public static List<Ward> wards = new ArrayList<>();

    public void getWards(Context ctx) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiBaseUrl))
                .GET()
                .build();
        
            HttpResponse response = client.send(
                request, HttpResponse.BodyHandlers.ofString());
            
            List<Ward> wards = objectMapper.readValue(response.body(), new TypeReference<List<Ward>>() {});
        
            ctx.json(wards);

        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
        }
        
    }

    public void getWardById(Context ctx) {
        String id = ctx.pathParam("id");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiBaseUrl +"/"+id))
                    .GET()
                    .build();
            
            HttpResponse response = client.send(
                request, HttpResponse.BodyHandlers.ofString());
            
            Ward ward = objectMapper.readValue(response.body(), Ward.class);

            ctx.json(ward);
        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
        }
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
