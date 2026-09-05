package co.wethinkcode.healthsafe.service;

import io.javalin.http.Context;

import java.util.Map;

public class AlertServiceHandler {

    public static int alertLevel = 1;

    public static void setAlertLevel(Context ctx){
        String level = ctx.pathParam("level");

        AlertServiceHandler.alertLevel = Integer.parseInt(level);

        ctx.status(200).json(Map.of("status","Level updated"));
    }
}
