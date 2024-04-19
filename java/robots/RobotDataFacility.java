package robots;

import constants.Constants;
import greenfield.Position;

import java.util.concurrent.ThreadLocalRandom;

// Useful methods for handling robot data
public class RobotDataFacility {
    private int counter;
    private static volatile RobotDataFacility instance;


    private RobotDataFacility() {
        // 50000 will be the first port to be assigned
        // Assignable ports should be a list of 100 elements
        this.counter = Constants.MIN_ROBOT_PORT - 1;
    }
    public static RobotDataFacility getInstance() {
        if (instance == null) {
            synchronized (RobotDataFacility.class) {
                if (instance == null) {
                    instance = new RobotDataFacility();
                }
            }
        }
        return instance;
    }
    public synchronized int getPort(){
        this.counter++;
        return this.counter;
    }
    public int getRandomID(){
        return ThreadLocalRandom.current().nextInt(Constants.MIN_ROBOT_ID, Constants.MAX_ROBOT_ID + 1);
    }
    synchronized public int getDistrict(Position position){
        int x = position.getX();
        int y = position.getY();
        System.out.println(x);
        System.out.println(y);
        if (position.getX() < 5 && position.getY() < 5){
            //D1
            return 1;
        } else if (position.getX() < 5 && position.getY() > 4) {
            //D2
            return 2;
        } else if (position.getX() > 4 && position.getY() < 5) {
            //D3
            return 3;
        }else{
            //D4
            return 4;
        }
    }
}
