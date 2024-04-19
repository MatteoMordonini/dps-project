package exceptions;

public class InvalidStatusFromServerException extends Exception{
    public InvalidStatusFromServerException (int status){
        super("Administrator Server send status " + status);
    }

}
