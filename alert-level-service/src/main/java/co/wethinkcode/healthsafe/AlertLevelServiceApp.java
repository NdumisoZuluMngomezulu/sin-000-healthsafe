package co.wethinkcode.healthsafe;

import io.javalin.Javalin;

import co.wethinkcode.healthsafe.service.AlertServiceHandler;

public class AlertLevelServiceApp {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7032);

        app.get("/health", ctx -> ctx.result("OK"));

        // Read the current Emergency Status (0-8, 8 = full Code Blue).
        app.get("/alert-level", AlertServiceHandler::getAlertLevel);

        // Update it, body: {"level": 0-8}.
        app.post("/alert-level", AlertServiceHandler::setAlertLevel);
        // Also accept a path-param form for convenience/testing.
        app.post("/alert-level/{level}", AlertServiceHandler::setAlertLevel);
    }
}
