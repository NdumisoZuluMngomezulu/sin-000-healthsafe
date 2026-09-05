package co.wethinkcode.healthsafe.mq;

import javax.jms.Connection;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.JMSException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Shared by every producer/consumer service that talks to the "staffing-events-topic"
 * ActiveMQ topic. Duplicated into each participating service's own source tree,
 * since these are independent Maven projects with no shared parent pom.
 */
public final class MqConfig {

    public static final String BROKER_URL = "tcp://localhost:61616";
    public static final String TOPIC = "staffing-events-topic";
    public static final ObjectMapper mapper = new ObjectMapper();
    public static ActiveMQConnectionFactory factory;

    private MqConfig() {
    }

    static {
        // Initialize the ActiveMQ Factory
        factory = new ActiveMQConnectionFactory(BROKER_URL);
    }

    public static Connection createConnection() throws JMSException {
        Connection connection = factory.createConnection();
        connection.start(); //always start the connection before returning
        return connection;
    }
}
