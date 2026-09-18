package co.wethinkcode.healthsafe;

import io.javalin.Javalin;

import co.wethinkcode.healthsafe.service.StaffingHandler;

public class StaffingServiceApp {

    public static void main(String[] args) {
        new StaffingHandler();
        Javalin app = Javalin.create().start(7033);

        app.get("/health", ctx -> ctx.result("OK"));

        // Generates/refreshes the on-call schedule for a ward: validates the
        // ward via ward-service, reads the current Emergency Status via
        // alert-level-service, then broadcasts the result on
        // staffing-events-topic (stage 3).
        app.post("/schedule/{wardId}", StaffingHandler::createSchedule);

        // Returns the most recently generated schedule for a ward.
        app.get("/schedule/{wardId}", StaffingHandler::getSchedule);
    }
}
