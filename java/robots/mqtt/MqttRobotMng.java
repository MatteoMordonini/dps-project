package robots.mqtt;


import com.google.gson.Gson;
import constants.Constants;
import org.eclipse.paho.client.mqttv3.*;
import robots.Statistic;

// Handle interaction with mqtt broker
public class MqttRobotMng {
    public static volatile MqttRobotMng instance;
    private MqttClient client;

    public final Object lock;
    public MqttRobotMng() {
        this.lock = new Object();
    }

    public static MqttRobotMng getInstance() {
        if (instance == null) {
            synchronized (MqttRobotMng.class) {
                if (instance == null) {
                    instance = new MqttRobotMng();
                }
            }
        }
        return instance;
    }
    // Setup and connect the mqtt client to the broker
    public void clientConfig(String brokerAddress, String clientID){
        try{
            this.client = new MqttClient(brokerAddress, clientID);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            this.client.connect(connOpts);
        } catch (
                MqttException e) {
            throw new RuntimeException(e);
        }
    }
    public void publish(Statistic statistic){
        synchronized (lock) {
            String jsonMessage = new Gson().toJson(statistic);
            MqttMessage message = new MqttMessage(jsonMessage.getBytes());
            String topic = Constants.getTopicByDistrict(statistic.getDistrict());
            try {
                this.client.publish(topic, message);
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
