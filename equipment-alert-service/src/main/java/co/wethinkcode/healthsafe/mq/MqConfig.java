package co.wethinkcode.healthsafe.mq;

import javax.jms.Connection;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.JMSException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Shared broker config, duplicated into every participating service's own
 * source tree since these are independent Maven projects with no shared
 * parent pom.
 *
 * QUEUE: equipment-failure-queue - ward-service publishes here when it
 * detects an equipment failure; this service is the guaranteed-delivery
 * consumer.
 */
public final class MqConfig {

    public static final String BROKER_URL = "tcp://localhost:61616";
    public static final String QUEUE = "equipment-failure-queue";
    public static final ObjectMapper mapper = new ObjectMapper();
    public static ActiveMQConnectionFactory factory;

    private MqConfig() {
    }

    static {
        factory = new ActiveMQConnectionFactory(BROKER_URL);
    }

    public static Connection createConnection() throws JMSException {
        Connection connection = factory.createConnection();
        connection.start();
        return connection;
    }
}
