package exceptions;

// This exception capture the case where no data is available from a robot present in the network
public class NoStatsAvailableException extends Exception{
    public NoStatsAvailableException(){
        super("Error: No stats available.");
    }
}
