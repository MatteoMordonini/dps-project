package data;


import javax.xml.bind.annotation.XmlRootElement;

// Class of the statistics message sent to Administrator Client
@XmlRootElement
public class StatsMessage {
    double average;

    public StatsMessage() {
    }

    public StatsMessage(double average) {
        this.average = average;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    @Override
    public String toString() {
        return Double.toString(average);

    }
}
