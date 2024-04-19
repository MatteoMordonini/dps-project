package robots;

import administrators.RobotInfo;
import greenfield.Position;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
// Data of the robot that can change during time
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RobotDynamicData {
    // Position of the robot on the city map
    @XmlElement( name = "robotPosition"  )
    private Position robotPosition;
    // List of other robots in the map
    @XmlElement( name = "robotInitDataList"  )
    private List<RobotInfo> robotInitDataList;
    @XmlElement( name = "robotDistrict"  )
    private int robotDistrict;

    public RobotDynamicData() {
    }

    public RobotDynamicData(Position robotPosition, List<RobotInfo> robotInitDataList, int robotDistrict) {
        this.robotPosition = robotPosition;
        this.robotInitDataList = robotInitDataList;
        this.robotDistrict = robotDistrict;
    }

     public Position getRobotPosition() {
        synchronized (this.robotPosition){
            return this.robotPosition;
        }
    }

    synchronized public void setRobotPosition(Position robotPosition) {
        this.robotPosition = robotPosition;
    }

     public List<RobotInfo> getRobotInitDataList() {
        synchronized (this.robotInitDataList){
            return this.robotInitDataList;
        }
    }

    synchronized public void setRobotInitDataList(List<RobotInfo> robotInitDataList) {
        this.robotInitDataList = robotInitDataList;
    }

    synchronized public int getRobotDistrict() {
        return robotDistrict;
    }

    synchronized public void setRobotDistrict(int robotDistrict) {
        this.robotDistrict = robotDistrict;
    }
    synchronized public void addRobotToTheList(RobotInfo robotInfo){
            this.robotInitDataList.add(robotInfo);
    }
}
