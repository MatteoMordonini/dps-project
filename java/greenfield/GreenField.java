package greenfield;
import java.util.Random;

// Operations for handling Greenfield map
public class GreenField {
    int[] districts;
    private final Object districtsLock;

    private static volatile GreenField instance;


    private GreenField() {
        // 50000 will be the first port to be assigned
        // Assignable ports should be a list of 100 elements
        this.districts = new int[4];
        this.districtsLock = new Object();
        initDistricts();
    }
    public static GreenField getInstance() {
        if (instance == null) {
            synchronized (GreenField.class) {
                if (instance == null) {
                    instance = new GreenField();
                }
            }
        }
        return instance;
    }
    private void initDistricts(){
        for (int i = 0; i < 4; i++){
            districts[i] = 0;
        }
    }

    // Add a robot in the correct district and return its position
    public Position addRobot(){
        int chosenDistrict;
        synchronized (districtsLock){
            chosenDistrict = getLessFullDistrict();
            // Add a robot in the minimum district
            districts[chosenDistrict] = districts[chosenDistrict] + 1;
        }
        return getRandomPositionByDistrict(chosenDistrict);
    }

    private int getLessFullDistrict() {
        synchronized (districtsLock){
            int minD = 0;
            for(int i = 1; i < 4; i++){
                if (districts[i] < districts[minD]){
                    minD = i;
                }
            }
            return minD;
        }
    }

    public void removeRobot(int district){
        synchronized (districtsLock){
            this.districts[district-1] = this.districts[district-1] - 1;
        }
    }
    private Position getRandomPositionByDistrict(int district){
        int minX = 0;
        int maxX = 0;
        int minY = 0;
        int maxY = 0;

        switch (district){
            case 0:
                minX = 0;
                maxX = 4;
                minY = 0;
                maxY = 4;
                break;
            case 1:
                minX = 0;
                maxX = 4;
                minY = 5;
                maxY = 9;
                break;
            case 2:
                minX = 5;
                maxX = 9;
                minY = 0;
                maxY = 4;
                break;
            case 3:
                minX = 5;
                maxX = 9;
                minY = 5;
                maxY = 9;
                break;
            default: break;
        }
        Random rand = new Random();
        int x = rand.nextInt((maxX - minX) + 1) + minX;
        int y = rand.nextInt((maxY - minY) + 1) + minY;
        return new Position(x, y);
    }

     public int[] getDistricts() {
        synchronized (districtsLock){
            return districts;
        }
    }
     public void printDistricts() {
        synchronized (districtsLock){
            for (int i = 0; i < 4; i++){
                int d = i+1;
                System.out.println("#robots in D"+ d + " = " + districts[i]);
            }
        }
    }
}
