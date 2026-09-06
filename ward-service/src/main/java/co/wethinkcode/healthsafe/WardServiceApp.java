package co.wethinkcode.healthsafe;

import io.javalin.Javalin;
import javax.jms.*;

import co.wethinkcode.healthsafe.mq.MqConfig;
import co.wethinkcode.healthsafe.service.WardServiceHandler;

public class WardServiceApp {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7031);

        app.get("/health", ctx -> ctx.result("OK"));

        // TODO (Provides lists of wards and departments.)
        // Add domain endpoints for ward-service here.
        subscribeToStaffingQueue();
    }

    private static void subscribeToStaffingQueue() {
        Thread listenerThread = new Thread(() -> {
            try {
                //connection to active mq
                Connection connection = MqConfig.createConnection();
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                Destination destination = session.createQueue("staffing-events-queue");
                MessageConsumer consumer = session.createConsumer(destination);
                
                System.out.println("MQ successfully listening to staffing events");
                //event driven callback listener
                consumer.setMessageListener(message -> {
                    try {
                        if (message instanceof TextMessage) {

                            WardServiceHandler.processSchedule((TextMessage) message);
                        }
                    } catch (Exception e) {
                        System.out.println("Failed to process message");
                    }
                });
                //start connection
                connection.start();
                System.out.println("MQ successfully listening on staffing-events-queue");
            } catch (JMSException e){
                System.out.println("sdcsd");
            }
        });

        listenerThread.start();
    }
}


// MQ TODO: subscribes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.healthsafe.mq.MqConfig)
// MQ TODO: publishes to ActiveMQ queue MqConfig.QUEUE when it detects an equipment failure on one of its wards.

/*
Map<String, Object> wardMap = (Map<String, Object>) scheduleMap.get("ward");
String wardId = null; 
if (wardMap != null) {
    // Adapt this field name to whatever your specific Ward model properties are named
    wardId = (String) wardMap.get("id"); 
}

// 4. Extract the nested list of 'assignedDoctors'
List<Map<String, Object>> doctorsList = (List<Map<String, Object>>) scheduleMap.get("assignedDoctors");

System.out.println("\n[Ward Service] --- New Schedule Event Interpreted ---");
System.out.println("Target Ward ID: " + wardId);
System.out.println("Department:     " + department);
System.out.println("Alert Level:    " + alertLevel);
System.out.println("Assigned Doctor Details:");

if (doctorsList != null) {
    for (Map<String, Object> doctor : doctorsList) {
        // Unpack individual fields from each doctor object block
        String doctorName = (String) doctor.get("name");
        String specialty = (String) doctor.get("specialty");
        System.out.println(" -> Doctor: " + doctorName + " (" + specialty + ")");
    }
        
}
*/