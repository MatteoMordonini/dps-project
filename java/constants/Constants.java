package constants;

// Some constant data of the robot and facility for districts
public class Constants {
    public static final String AS_ADDRESS = "http://localhost:1337/";
    public static final int MIN_ROBOT_ID = 0;
    public static final int MAX_ROBOT_ID = 99;
    public static final int MIN_ROBOT_PORT = 50000;
    public static final int MAX_ROBOT_PORT = 50099;
    public static final String MQTT_BROKER_ADDRESS = "tcp://localhost:1883";

    public static String getTopicByDistrict(int district) {
        return "greenfield/pollution/district" + district;
    }

}
