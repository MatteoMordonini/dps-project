package administrators;

import data.StatsMessage;
import exceptions.*;
import robots.Statistic;
import simulator.Measurement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StatsManager {
    private static volatile StatsManager instance;
    private Object lock;
    // List of measurements for each robot
    private HashMap<Integer, List<Measurement>> statsDB;


    private StatsManager() {
        this.lock = new Object();
        this.statsDB = new HashMap<>();
    }

    public static StatsManager getInstance() {
        if (instance == null) {
            synchronized (StatsManager.class) {
                if (instance == null) {
                    instance = new StatsManager();
                }
            }
        }
        return instance;
    }

    // From the newest to the oldest measurements
    public StatsMessage getLastNStatsByRobot(String id, int n) throws NoStatsYetForThisRobotException, NotEnoughStatsException {
        List<Measurement> result = new ArrayList<>();
        synchronized (lock){
            if (!this.statsDB.containsKey(Integer.parseInt(id))){
                throw new NoStatsYetForThisRobotException();
            }
            else {
                List<Measurement> robotMeasurements = statsDB.get(Integer.parseInt(id));
                if (n > robotMeasurements.size()){
                    throw new NotEnoughStatsException();
                }
                int listSize = robotMeasurements.size();
                for (int i = listSize-1; i > listSize-1-n; i--){
                    result.add(robotMeasurements.get(i));
                }
            }
        }
        double average = computeAverage(result);

        return new StatsMessage(average);
    }

    private double computeAverage(List<Measurement> list) {
        double sum = 0;
        int count = 0;
        for (Measurement m : list){
            count++;
            sum = sum + m.getValue();
        }
        double average = sum / count;
        return average;
    }

    public StatsMessage getStatsBwTs(String t1, String t2) throws NoStatsAvailableException, WrongTimeStampException {
        if (Long.parseLong(t1) < 0 || Long.parseLong(t2) < 0){
            throw new WrongTimeStampException();
        }
        List<Measurement> result = new ArrayList<>();
        synchronized (lock){
            for (List<Measurement> mList : statsDB.values()){
                for (Measurement m : mList){
                    long currentTs = m.getTimestamp();
                    if (currentTs >= Long.parseLong(t1) && currentTs <= Long.parseLong(t2)){
                        result.add(m);
                    }
                }
            }
        }
        if (result.isEmpty()){
            throw new NoStatsAvailableException();
        }
        double average = computeAverage(result);
        return new StatsMessage(average);
    }
    public void insertStat(Statistic statistic){
        synchronized (lock){
            int robotID = statistic.getRobotId();
            List<Measurement> avgList = statistic.getAvgList();

            if(!statsDB.containsKey(robotID)){
                statsDB.put(robotID, avgList);
            }else {
                List<Measurement> tempList = statsDB.get(robotID);
                for (Measurement m : avgList){
                    tempList.add(m);
                }
            }
        }
    }
}
