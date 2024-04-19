package administrators;

import com.google.gson.Gson;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import constants.Constants;
import org.eclipse.paho.client.mqttv3.*;
import robots.Statistic;

import java.io.IOException;

public class AdministratorServer {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServerFactory.create(Constants.AS_ADDRESS);
        server.start();
        subscribeToMQTTBroker();

        System.out.println("Server running!");
        System.out.println("Server started on:" + Constants.AS_ADDRESS);

        System.out.println("Hit return to stop...");
        System.in.read();
        System.out.println("Stopping server");
        server.stop(0);
        System.out.println("Server stopped");
    }

    private static void subscribeToMQTTBroker() {
        try {
            MqttClient client = new MqttClient(Constants.MQTT_BROKER_ADDRESS, "Administrator_Server");
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            client.connect(connOpts);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("Error: connection lost.\nCause message: " + cause.getMessage()+ "\nThread PID: " + Thread.currentThread().getId());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String receivedMessage = new String(message.getPayload());
                    Statistic statistic = new Gson().fromJson(receivedMessage, Statistic.class);
                    System.out.println("Received the statistic:");
                    System.out.println(statistic.toString());
                    StatsManager statsManager = StatsManager.getInstance();
                    statsManager.insertStat(statistic);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
            client.subscribe(Constants.getTopicByDistrict(1),2);
            client.subscribe(Constants.getTopicByDistrict(2),2);
            client.subscribe(Constants.getTopicByDistrict(3),2);
            client.subscribe(Constants.getTopicByDistrict(4),2);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }

    }

}
