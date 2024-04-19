package exceptions;

public class RobotPortAlreadyPresentException extends Exception{
        public RobotPortAlreadyPresentException(){
                super("Error: A robot with the same port is already present in the network.");
        }
}
