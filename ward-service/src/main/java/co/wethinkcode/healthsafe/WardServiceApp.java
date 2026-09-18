package co.wethinkcode.healthsafe;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import io.javalin.Javalin;

import co.wethinkcode.healthsafe.mq.MqConfig;
import co.wethinkcode.healthsafe.service.WardServiceHandler;

public class WardServiceApp {

    public static void main(String[] args) {
        // Populate our ward/department list from ingestion-service before serving.
        WardServiceHandler.loadWardsFromIngestion();

        Javalin app = Javalin.create().start(7031);

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/wards", WardServiceHandler::getWards);
        app.get("/wards/{id}", WardServiceHandler::getWardById);
        app.get("/departments", WardServiceHandler::departments);

        app.post("/wards/{id}/equipment", WardServiceHandler::reportEquipment);
        app.get("/wards/{id}/equipment", WardServiceHandler::getEquipment);

        // Stage 3: react to staffing-service's broadcasts instead of polling it.
        subscribeToStaffingTopic();
    }

    private static void subscribeToStaffingTopic() {
        Thread listenerThread = new Thread(() -> {
            try {
                Connection connection = MqConfig.createConnection();
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                Destination destination = session.createTopic(MqConfig.TOPIC);
                MessageConsumer consumer = session.createConsumer(destination);

                consumer.setMessageListener(message -> {
                    try {
                        if (message instanceof TextMessage textMessage) {
                            WardServiceHandler.applyStaffingEvent(textMessage.getText());
                        }
                    } catch (Exception e) {
                        System.err.println("ward-service: failed to process staffing event - " + e.getMessage());
                    }
                });

                connection.start();
                System.out.println("ward-service: listening on topic " + MqConfig.TOPIC);
            } catch (JMSException e) {
                System.err.println("ward-service: could not connect to broker ("
                        + MqConfig.BROKER_URL + ") - " + e.getMessage()
                        + ". Start it with `cd common && docker compose up -d`.");
            }
        }, "staffing-topic-listener");

        listenerThread.setDaemon(true);
        listenerThread.start();
    }
}
