package co.wethinkcode.healthsafe;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import co.wethinkcode.healthsafe.mq.MqConfig;
import io.javalin.Javalin;

public class EquipmentAlertServiceApp {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7034);

        app.get("/health", ctx -> ctx.result("OK"));

        // TODO (Uses a Queue to guarantee delivery of critical medical equipment failure alerts.)
        // Mechanism: ActiveMQ Queue (guaranteed delivery)
    }

    private static void subscribeToWardQueue(){
        Thread listenerThread = new Thread(() -> {
            try {
                //connect to active mq
                Connection connection = MqConfig.createConnection();
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                Destination destination = session.createQueue("ward-service-queue");
                MessageConsumer consumer = session.createConsumer(destination);

                System.out.println("MQ successfully listening to ward-service-queue");

                consumer.setMessageListener(message -> {
                    try {
                        if (message instanceof TextMessage) {
                            System.out.println("got it");
                        }
                    } catch (Exception e){
                        System.out.println("Failed to process message");
                    }
                });

            } catch (Exception e){
                System.out.println("Failed to process message");
            }
        });

        listenerThread.start();
}
}



// MQ TODO: consumes ActiveMQ queue MqConfig.QUEUE at MqConfig.BROKER_URL (see co.wethinkcode.healthsafe.mq.MqConfig)
// Producer: ward-service publishes here when it detects an equipment failure on one of its wards.
