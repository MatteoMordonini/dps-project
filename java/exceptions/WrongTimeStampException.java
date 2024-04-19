package exceptions;

public class WrongTimeStampException extends Exception{
    public WrongTimeStampException(){
        super("Error: The selected timestamps are not valid.");
    }
}
