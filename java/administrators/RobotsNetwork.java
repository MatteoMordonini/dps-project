package administrators;
import exceptions.RobotIDAlreadyPresentException;
import exceptions.RobotNotPresentException;
import exceptions.RobotPortAlreadyPresentException;
import greenfield.GreenField;
import greenfield.Position;
import robots.RobotDataFacility;
import robots.RobotInitData;

import java.util.ArrayList;
import java.util.List;

// Singleton that handle most of the robots network for AS
public class RobotsNetwork {
    private static volatile RobotsNetwork instance;
    private List<RobotInfo> robotsList;
    public final Object listLock;
    private GreenField greenField;
    private RobotDataFacility robotDataFacility;
    private RobotsNetwork() {
        this.robotsList = new ArrayList<>();
        this.listLock = new Object();
        this.greenField = GreenField.getInstance();
        this.robotDataFacility = RobotDataFacility.getInstance();
    }

    public static RobotsNetwork getInstance() {
        if (instance == null) {
            synchronized (RobotsNetwork.class) {
                if (instance == null) {
                    instance = new RobotsNetwork();
                }
            }
        }
        return instance;
    }

    public List<RobotInfo> getRobotsList() {
        return this.robotsList;
    }

    public Position addRobot(RobotInitData newRobot) throws RobotIDAlreadyPresentException, RobotPortAlreadyPresentException {
        synchronized (listLock) {
            // Throws exception if a robot with the same id or port is yet in the network
            hasSameIdorPort(newRobot);
            // Get a position for the robot and add it to the list
            Position position = getNewPosition();
            System.out.println("Current districts contents:");
            greenField.printDistricts();
            int d = this.robotDataFacility.getDistrict(position);
            String district = Integer.toString(d);
            RobotInfo robot = new RobotInfo(newRobot, district);
            System.out.println("Added robot with\n"
                    + "id = " + robot.getRobotInitData().getId()
                    + "\nport = " + robot.getRobotInitData().getPort()
                    + "\ndistr = " + robot.getDistrict()
                    + "\npos = " + position
                    + "\nand address = " + robot.getRobotInitData().getAddress());
            this.robotsList.add(robot);
            return position;
            }
    }

    private void hasSameIdorPort(RobotInitData newRobot) throws RobotIDAlreadyPresentException, RobotPortAlreadyPresentException {
        synchronized (listLock){
            for (RobotInfo r : robotsList){
                if (r.getRobotInitData().getId() == newRobot.getId()){
                    throw new RobotIDAlreadyPresentException();
                }
                if (r.getRobotInitData().getPort() == newRobot.getPort()){
                    throw new RobotPortAlreadyPresentException();
                }
            }

        }
    }

    private Position getNewPosition() {
        // add the robot in the greenfield map and give its assigned position
        return greenField.addRobot();
    }

    public void deleteRobot(String id) throws RobotNotPresentException {
        synchronized (listLock) {
            boolean found = false;
            RobotInfo toRemove = new RobotInfo();
            for (RobotInfo r : this.robotsList) {
                if (r.getRobotInitData().getId() == Integer.parseInt(id)) {
                    // Decrese associated district size
                    greenField.removeRobot(Integer.parseInt(r.getDistrict()));
                    toRemove = r;
                    found = true;
                }
            }
            if (found == false){
                throw new RobotNotPresentException();
            }
            // Remove robot from the list
            robotsList.remove(toRemove);
        }
    }

    public void updateDistrict(String id, String district) throws RobotNotPresentException{
        synchronized (listLock){
            boolean found = false;
            for (RobotInfo r : robotsList){
                if (r.getRobotInitData().getId() == Integer.parseInt(id)){
                    r.setDistrict(district);
                    found = true;
                }
            }
            if (found){
                throw new RobotNotPresentException();
            }
        }
    }
    public void printList() {
        synchronized (listLock){
            for(RobotInfo r : this.robotsList){
                System.out.println(r);
            }
        }
    }
    public boolean isPresent(int id){
        boolean result = false;
        synchronized (listLock){
            for(RobotInfo r : this.robotsList){
                if (id == r.getRobotInitData().getId()){
                    result = true;
                }
            }
        }
        return result;
    }
}
