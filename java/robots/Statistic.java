package robots;

import simulator.Measurement;

import java.util.List;

public class Statistic {
    int district;
    int robotId;
    List<Measurement> avgList;
    public void getLastStats(){

    }

    public Statistic(List<Measurement> avgList, int robotId, int district) {
        this.district = district;
        this.robotId = robotId;
        this.avgList = avgList;
    }

    public int getDistrict() {
        return district;
    }

    public void setDistrict(int district) {
        this.district = district;
    }

    public int getRobotId() {
        return robotId;
    }

    public void setRobotId(int robotId) {
        this.robotId = robotId;
    }

    public List<Measurement> getAvgList() {
        return avgList;
    }

    public void setAvgList(List<Measurement> avgList) {
        this.avgList = avgList;
    }

    @Override
    public String toString() {
        return "id = " + this.robotId + ", district = " + this.district + "\n" + "Avg list = " + this.avgList + "\n";
    }
}
