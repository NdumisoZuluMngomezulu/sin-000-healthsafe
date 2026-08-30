package co.wethinkcode.healthsafe;

import io.javalin.Javalin;
import io.javalin.http.Context;

public class AlertLevelServiceApp {
    public static int alertLevel = 0;

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7032);

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/alert-level", ctx -> {
            ctx.json(AlertLevelServiceApp.alertLevel);
        });

        app.post("/alert-level", AlertLevelServiceApp::setAlertLevel);

        // TODO (Tracks the hospital Emergency Status (0-8, 8 = full Code Blue).)
        // Add domain endpoints for alert-level-service here.
    }

    public static void setAlertLevel(Context ctx){
        String level = ctx.pathParam("level");

        AlertLevelServiceApp.alertLevel = Integer.parseInt(level);
    }
}
