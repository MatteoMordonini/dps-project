package robots;

import administrators.RobotInfo;
import exceptions.RobotNotPresentException;

import java.util.ArrayList;
import java.util.List;

// Singleton that manages robot data across the robot threads
public class RobotDataManager {
    private static volatile RobotDataManager instance;
    private RobotDynamicData robotDynamicData;
    private RobotInitData robotInitData;
    private boolean inMaintenance;
    private boolean needMaintenance;
    private long maintenanceTs;
    private List<RobotInitData> sendOkList;
    private final Object mLock;
    // If i'm in controlled quitting, i can't permit a random maintenance trigger
    private final Object quittingLock;
    private final Object robotsListLock;
    private MessageCounter messageCounter;
    private RobotDataManager() {
        this.inMaintenance = false;
        this.needMaintenance = false;
        this.mLock = new Object();
        this.quittingLock = new Object();
        this.robotsListLock = new Object();
        this.sendOkList = new ArrayList<>();
    }
    public static RobotDataManager getInstance() {
        if (instance == null) {
            synchronized (RobotDataManager.class) {
                if (instance == null) {
                    instance = new RobotDataManager();
                }
            }
        }
        return instance;
    }

    public Object getRobotsListLock() {
        return robotsListLock;
    }

    public Object getQuittingLock() {
        return quittingLock;
    }

    synchronized public void setRobotDynamicData(RobotDynamicData robotDynamicData) {
        this.robotDynamicData = robotDynamicData;
    }

    public RobotDynamicData getRobotDynamicData() {
        return robotDynamicData;
    }

    public RobotInitData getRobotInitData() {
        return robotInitData;
    }

    public void setRobotInitData(RobotInitData robotInitData) {
        this.robotInitData = robotInitData;
    }
    public void insertRobot(RobotInfo robotInfo) {
        synchronized (robotsListLock){
            this.robotDynamicData.getRobotInitDataList().add(robotInfo);
        }
    }
     public void removeRobot(int robotId) throws RobotNotPresentException {
        synchronized (robotsListLock){
            boolean found = false;
            int listSize = this.robotDynamicData.getRobotInitDataList().size();
            List<RobotInfo> temp = this.robotDynamicData.getRobotInitDataList();
            int i = 0;
            while(found == false &&  i < listSize){
                if (temp.get(i).getRobotInitData().getId() == robotId){
                    temp.remove(i);
                    found = true;
                }
                i++;
            }
            if (found == false){
                throw new RobotNotPresentException();
            }
        }
        System.out.println("Removed robot with id =" + robotId + ".");
    }

     public boolean isInMaintenance() {
        synchronized (mLock){
            return inMaintenance;
        }
    }

     public void setInMaintenance(boolean inMaintenance) {
         synchronized (mLock){
        this.inMaintenance = inMaintenance;
         }
    }

    public boolean isNeedMaintenance() {
             synchronized (mLock){
        return needMaintenance;
             }
    }

    public void setNeedMaintenance(boolean needMaintenance) {
        synchronized (mLock){
            this.needMaintenance = needMaintenance;
        }
    }

    public long getMaintenanceTs() {
        return maintenanceTs;
    }

    public void setMaintenanceTs(long maintenanceTs) {
        this.maintenanceTs = maintenanceTs;
    }
    public void addToSendOkList(RobotInitData r){
        synchronized (sendOkList){
            this.sendOkList.add(r);
        }
    }

    public MessageCounter getMessageCounter() {
        return messageCounter;
    }

    public void setMessageCounter(MessageCounter messageCounter) {
        this.messageCounter = messageCounter;
    }
    public List<RobotInitData> getAllAndClean(){
        synchronized (sendOkList){
            List temp = new ArrayList<>(this.sendOkList);
            this.sendOkList.clear();
            return temp;
        }

    }

    public void printList() {
        synchronized (robotsListLock){
            for(RobotInfo r : this.getRobotDynamicData().getRobotInitDataList()){
                System.out.println(r);
            }
        }
    }
}
