package co.wethinkcode.healthsafe.service;

import java.util.ArrayList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import co.wethinkcode.healthsafe.model.Equipment;
import co.wethinkcode.healthsafe.model.Ward;
import co.wethinkcode.healthsafe.mq.MqConfig;

public class WardServiceHandler {
    public static List<Ward> wards = new ArrayList<>();

    public static void publishToWardQueue(Equipment equipment){
        try (Connection connection = MqConfig.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
            
            Destination destination = session.createQueue("ward-service-queue");
            MessageProducer producer = session.createProducer(destination);

            String jsonPayload = MqConfig.mapper.writeValueAsString(equipment);
            TextMessage message = session.createTextMessage(jsonPayload);

            producer.send(message);
            System.out.println("MQ has sent event " + equipment.getName() + " details to Equipment service");

            
        } catch (Exception e) {
            System.err.println("MQ failed to send equipment information");
        }
    }
}
