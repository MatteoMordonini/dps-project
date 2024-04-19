package administrators;

import administrators.RobotInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class RobotsListMessage {
    public RobotsListMessage() {
    }

    private List<RobotInfo> robotsList;

    public RobotsListMessage(List<RobotInfo> robotsList) {
        this.robotsList = robotsList;
    }

    public List<RobotInfo> getRobotsList() {
        return robotsList;
    }

    public void setRobotsList(List<RobotInfo> robotsList) {
        this.robotsList = robotsList;
    }
}
