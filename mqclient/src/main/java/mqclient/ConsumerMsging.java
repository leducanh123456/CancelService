package mqclient;

import javax.jms.Connection;

import javax.jms.ConnectionFactory;

import javax.jms.Destination;

import javax.jms.JMSException;

import javax.jms.Message;

import javax.jms.MessageConsumer;

import javax.jms.Session;

import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

public class ConsumerMsging {

	private static String url = "failover://tcp://192.168.1.211:61616";

	// Name of the queue we will receive messages from

	private static String subject = "TESTQUEUE1";

	public static void main(String[] args) throws JMSException {

		// Getting JMS connection from the server

		ConnectionFactory connectionFactory

				= new ActiveMQConnectionFactory(url);

		Connection connection = connectionFactory.createConnection();

		connection.start();

		// Creating session for seding messages

		Session session = connection.createSession(false,

				Session.AUTO_ACKNOWLEDGE);

		// Getting the queue ‘TESTQUEUE’

		Destination destination = session.createQueue(subject);

		// MessageConsumer is used for receiving (consuming) messages

		MessageConsumer consumer = session.createConsumer(destination);

		while (true) {

			Message message = consumer.receive();

			// There are many types of Message and TextMessage

			// is just one of them. Producer sent us a TextMessage

			// so we must cast to it to get access to its .getText()

			// method.

			if (message instanceof TextMessage) {

				TextMessage textMessage = (TextMessage) message;

				System.out.println("Received message ‘"

						+ textMessage.getText() + "'");
			}

		}

		// connection.close();

	}

}
