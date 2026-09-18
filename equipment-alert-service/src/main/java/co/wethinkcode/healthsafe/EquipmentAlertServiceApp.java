package co.wethinkcode.healthsafe;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import co.wethinkcode.healthsafe.mq.MqConfig;
import co.wethinkcode.healthsafe.service.EquipmentAlertHandler;
import io.javalin.Javalin;

public class EquipmentAlertServiceApp {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7034);

        app.get("/health", ctx -> ctx.result("OK"));

        // All equipment failure alerts received so far - handy for
        // confirming end-to-end delivery without watching the broker console.
        app.get("/alerts", EquipmentAlertHandler::listAlerts);

        subscribeToWardQueue();
    }

    private static void subscribeToWardQueue() {
        Thread listenerThread = new Thread(() -> {
            try {
                Connection connection = MqConfig.createConnection();
                // CLIENT_ACKNOWLEDGE: only acknowledge a message once we've
                // successfully processed it, so a failure/crash leaves it on
                // the queue to be redelivered - the guaranteed-delivery
                // contract for equipment-failure-queue.
                Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

                Destination destination = session.createQueue(MqConfig.QUEUE);
                MessageConsumer consumer = session.createConsumer(destination);

                consumer.setMessageListener(message -> {
                    try {
                        if (message instanceof TextMessage textMessage) {
                            EquipmentAlertHandler.handle(textMessage.getText());
                        }
                        message.acknowledge();
                    } catch (Exception e) {
                        System.err.println("equipment-alert-service: failed to process alert, "
                                + "leaving it unacknowledged for redelivery - " + e.getMessage());
                    }
                });

                connection.start();
                System.out.println("equipment-alert-service: listening on queue " + MqConfig.QUEUE);
            } catch (JMSException e) {
                System.err.println("equipment-alert-service: could not connect to broker ("
                        + MqConfig.BROKER_URL + ") - " + e.getMessage()
                        + ". Start it with `cd common && docker compose up -d`.");
            }
        }, "equipment-queue-listener");

        listenerThread.setDaemon(true);
        listenerThread.start();
    }
}
