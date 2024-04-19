package exceptions;

public class NotEnoughStatsException extends Throwable {
    public NotEnoughStatsException(){
        super("Error: Not enough stats available.");
    }

}
