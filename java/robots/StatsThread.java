package robots;

import robots.mqtt.MqttRobotMng;
import simulator.Buffer;
import simulator.BufferImpl;
import simulator.Measurement;

import java.util.List;

//Receives statistics from the sensor
public class StatsThread extends Thread{
    Buffer buffer;
    List<Measurement> avgList;
    Statistic statistic;
    RobotInitData robotInitData;
    RobotDynamicData robotDynamicData;
    MqttRobotMng mqttRobotMng;
    public StatsThread(BufferImpl bufferImpl, RobotInitData robotInitData, RobotDynamicData robotDynamicData) {
        this.buffer = bufferImpl;
        this.robotInitData = robotInitData;
        this.robotDynamicData = robotDynamicData;
        this.mqttRobotMng = MqttRobotMng.getInstance();
    }

    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(15000);
                // Get list of averages from the buffer
                this.avgList = this.buffer.readAllAndClean();
                // Create statistic to be sended to MQTT broker
                this.statistic = new Statistic(this.avgList,
                        this.robotInitData.getId(),
                        this.robotDynamicData.getRobotDistrict());

                if (this.avgList.isEmpty() == false){
                    System.out.print("Sending to the mqtt broker this statistic:\n" + this.statistic);
                    // send the list to the mqtt broker
                    mqttRobotMng.publish(this.statistic);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }

    }
}
