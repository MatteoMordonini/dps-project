package administrators;

import administrators.RobotInfo;
import greenfield.Position;
import robots.RobotInitData;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class AddRobotMessage {

    private Position position;
    private List<RobotInfo> robotsList;
    private AddRobotMessage(){}

    public AddRobotMessage(Position position, List<RobotInfo> robotsList) {
        this.position = position;
        this.robotsList = robotsList;
    }

    public Position getPosition() {
        return position;
    }

    public List<RobotInfo> getRobotsList() {
        return robotsList;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setRobotsList(List<RobotInfo> robotsList) {
        this.robotsList = robotsList;
    }
}
