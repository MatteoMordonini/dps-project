package administrators;

import data.StatsMessage;
import exceptions.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("stats")
public class StatsServices {
    public static String STATS_SERVICES_PATH = "stats";
    // Return the average of the last n air pollution levels captured by a given robot
    @Path("getLastNStatsByRobot/{id}/{n}")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getLastNStatsByRobot(@PathParam("id") String id, @PathParam("n") int n){
        try {
            if (!RobotsNetwork.getInstance().isPresent(Integer.parseInt(id))){
                throw new RobotNotPresentException();
            }
            StatsMessage result = StatsManager.getInstance().getLastNStatsByRobot(id, n);
            System.out.println("Sending stats: " + result.getAverage());
            return Response.ok(result).build();
        } catch (RobotNotPresentException | NotEnoughStatsException | NoStatsYetForThisRobotException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    // Return the average of the air pollution levels captured by every robot between two timestamps
    @Path("getStatsBwTs/{t1}/{t2}")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getStatsBwTs(@PathParam("t1") String t1, @PathParam("t2") String t2) {
        try {
            StatsMessage result = StatsManager.getInstance().getStatsBwTs(t1, t2);
            System.out.println("Sending stats: " + result.toString());
            return Response.ok(result).build();
        } catch (NoStatsAvailableException e) {
            System.out.println("Error: " + e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (WrongTimeStampException e) {
            System.out.println("Error: " + e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }
}
