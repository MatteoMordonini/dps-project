package exceptions;

public class NoStatsYetForThisRobotException extends Exception {
    public NoStatsYetForThisRobotException(){
        super("Error: No stats yet available for this robot.");
    }
}
