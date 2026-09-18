package co.wethinkcode.healthsafe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import co.wethinkcode.healthsafe.model.Ward;
import co.wethinkcode.healthsafe.service.CSVLoader;
import io.javalin.Javalin;

public class IngestionServiceApp {
    public static List<Ward> wards = new ArrayList<>();

    public static void main(String[] args) {

        CSVLoader loader = new CSVLoader();
        try {
            wards = loader.getWards();
            System.out.println("Loaded " + wards.size() + " cleaned ward records from wards-outdated.csv");
        } catch (IOException e) {
            System.out.println("Could not load wards: " + e.getMessage());
        }

        Javalin app = Javalin.create().start(7030);

        app.get("/health", ctx -> ctx.result("OK"));

        // Cleaned ward records for ward-service to consume.
        app.get("/wards", ctx -> ctx.json(wards));

        app.get("/wards/{id}", ctx -> {
            String id = ctx.pathParam("id").toUpperCase();
            Ward match = wards.stream()
                    .filter(w -> w.getWardId().equals(id))
                    .findFirst()
                    .orElse(null);
            if (match == null) {
                ctx.status(404).json(Map.of("error", "unknown ward " + id));
                return;
            }
            ctx.json(match);
        });
    }
}

