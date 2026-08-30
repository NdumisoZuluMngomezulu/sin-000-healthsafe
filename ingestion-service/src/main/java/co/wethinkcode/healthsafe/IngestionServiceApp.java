package co.wethinkcode.healthsafe;

import co.wethinkcode.healthsafe.service.CSVLoader;

import java.util.List;
import java.io.IOException;

import co.wethinkcode.healthsafe.model.Ward;

import io.javalin.Javalin;

public class IngestionServiceApp {

    public static void main(String[] args) {
        
        CSVLoader loader = new CSVLoader();
        try {
            List<Ward> wards = loader.getWards();
        } catch (IOException e) {
            System.out.println("Could not load wards" + e.getMessage();)
        }
        
        Javalin app = Javalin.create().start(7030);

        app.get("/wards", ctx -> {ctx.json(wards);});

        app.get("/health", ctx -> ctx.result("OK"));

        // TODO: read and clean src/main/resources/wards-outdated.csv (wards, wings, specialist departments data —
        // trim whitespace, fix casing, normalize dates/booleans) and expose the
        // cleaned records here for the other services to consume.
    }
}
