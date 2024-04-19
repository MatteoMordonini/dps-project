package administrators;

import robots.RobotInitData;

import javax.xml.bind.annotation.XmlRootElement;


// Handle important for operations single robot data
@XmlRootElement
public class RobotInfo {
    private RobotInitData robotInitData;
    private String district;

    public RobotInfo() {
    }

    public RobotInfo(RobotInitData robotInitData, String district) {
        this.robotInitData = robotInitData;
        this.district = district;
    }

    public RobotInitData getRobotInitData() {
        return robotInitData;
    }

    public void setRobotInitData(RobotInitData robotInitData) {
        this.robotInitData = robotInitData;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    @Override
    public String toString() {
        return robotInitData.toString() + ", DISTRICT:" + district;

    }
}
