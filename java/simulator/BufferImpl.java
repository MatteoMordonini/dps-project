package simulator;

import java.util.ArrayList;
import java.util.List;

public class BufferImpl implements Buffer{
    private ArrayList<Measurement> buffer;
    // List of averages
    private List<Measurement> avgList;
    private int maxMeasurements;
    private double overlapFactor;
    int id = 0;
    public final Object lock;

    public BufferImpl() {
        buffer = new ArrayList<>();
        avgList = new ArrayList<>();
        this.lock = new Object();
        maxMeasurements = 8;
        overlapFactor = 0.5;
    }

    @Override
    public void addMeasurement(Measurement m) {
        synchronized (lock) {
            buffer.add(m);
            if (buffer.size() == 8) {
                avg();
                slide();
            }
        }
    }

    @Override
    public List<Measurement> readAllAndClean() {
        ArrayList<Measurement> avgListCopy = new ArrayList<>(avgList);
        avgList.clear();
        return avgListCopy;
    }

    // Compute the avg and add it to avg list
    public void avg(){
        synchronized (lock) {
            double sum = 0;
            for (Measurement m : buffer) {
                sum = sum + m.getValue();
            }
            double avg = sum / buffer.size();
        this.id++;
        avgList.add(new Measurement(Integer.toString(this.id), "PM10", avg, System.currentTimeMillis()));
        }

    }
    // Make the sliding window process (remove first 4 elements from the buffer)
    public void slide(){
        synchronized (lock){
            for (int i = 0; i < maxMeasurements * overlapFactor; i++) {
                buffer.remove(0);
            }
        }
    }
}
