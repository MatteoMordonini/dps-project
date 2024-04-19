package exceptions;

public class RobotAlreadyPresentException extends Exception {
    public RobotAlreadyPresentException() {
        super("Error: Another robot with the same ID is already present.\nExiting...");
    }
}
