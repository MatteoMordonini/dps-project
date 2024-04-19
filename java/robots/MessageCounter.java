package robots;

import administrators.RobotInfo;

import java.util.ArrayList;

// Count messages during maintenance queue
public class MessageCounter {
    // If limit is equals to currentResponses, means that the robot has received all the responses he needs
    private int                         limit;
    private int                         currentResponses;
    private final Object                lock;
    private ArrayList<RobotInfo> askOkList;

    public MessageCounter(int limit, ArrayList<RobotInfo> askOkList) {
        this.limit = limit;
        this.currentResponses = 0;
        this.lock = new Object();
        this.askOkList = askOkList;
    }

    public void incrCounter(){
        synchronized (lock){
            this.currentResponses++;
            this.lock.notifyAll();
        }
    }

    public synchronized Object getLock() {
        return lock;
    }

    public synchronized int getLimit() {
        return this.limit;
    }

    public synchronized int getCurrentResponses() {
        synchronized (lock) {
            return this.currentResponses;
        }
    }

    public ArrayList<RobotInfo> getAskOkList() {
        return askOkList;
    }
}
