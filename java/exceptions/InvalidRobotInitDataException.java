package exceptions;

public class InvalidRobotInitDataException extends Exception{
    public InvalidRobotInitDataException() {
            super("Invalid id or port for joining the network.");
    }
}
