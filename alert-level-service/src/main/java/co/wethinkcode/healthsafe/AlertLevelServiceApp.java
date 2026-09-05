package co.wethinkcode.healthsafe;

import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.Map;

import co.wethinkcode.healthsafe.service.AlertServiceHandler;

public class AlertLevelServiceApp {
    public static int alertLevel = 0;

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7032);

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/alert-level", ctx -> {
            ctx.json(AlertServiceHandler.alertLevel);
        });

        app.post("/alert-level", AlertServiceHandler::setAlertLevel);

        // TODO (Tracks the hospital Emergency Status (0-8, 8 = full Code Blue).)
        // Add domain endpoints for alert-level-service here.
    }

    public static void setAlertLevel(Context ctx){
        String level = ctx.pathParam("level");

        AlertLevelServiceApp.alertLevel = Integer.parseInt(level);

        ctx.status(200).json(Map.of("status","Level updated"));
    }
}
