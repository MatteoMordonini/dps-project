package exceptions;

public class RobotIDAlreadyPresentException extends Exception{
    public RobotIDAlreadyPresentException(){
        super("Error: A robot with the same ID is already present in the network.");
    }
}
