package robots;

import javax.xml.bind.annotation.XmlRootElement;
// Data configured by the user or by the RobotDataFacility that must be sended to the AS
@XmlRootElement
public class RobotInitData {
    private int id;
    private int port;
    private String address;
    public RobotInitData(){
        this.id = -1;
        this.port = -1;
        this.address = "localhost:";

    }
    public RobotInitData(int id, int port, String address) {
        this.id = id;
        this.port = port;
        this.address = address;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "ID: " + this.id + ", PORT: " + this.port + ", ADDRESS: " + this.address;

    }
}
