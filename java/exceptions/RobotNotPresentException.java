package exceptions;

public class RobotNotPresentException extends Exception{
    public RobotNotPresentException(){
        super("Error: This robot is not present in the Network.");
    }
}
