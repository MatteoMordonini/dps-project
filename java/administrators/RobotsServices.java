package administrators;

import exceptions.RobotIDAlreadyPresentException;
import exceptions.RobotNotPresentException;
import exceptions.RobotPortAlreadyPresentException;
import greenfield.Position;
import robots.RobotInitData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("robots")
public class RobotsServices {
    public static String ROBOT_SERVICES_PATH = "robots";
    RobotsNetwork rn = RobotsNetwork.getInstance();

    // Return the list of robots in the network
    @Path("getRobotsList")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getRobotsList(){
        List<RobotInfo> result = RobotsNetwork.getInstance().getRobotsList();
        System.out.println("Sending robots list: " + result);
        return Response.ok(new RobotsListMessage(result), MediaType.APPLICATION_JSON).build();
    }

/* Insert a new robot in the network
    *  if its id and port are unique, then
    *  Send back to the robot: starting position, list of robots containing
    *  for each one id, ip, port
    *  */

    @Path("add")
    @POST
    @Consumes({"application/json", "application/xml"})
    @Produces({"application/json", "application/xml"})
    public Response addRobot(RobotInitData robot){
        try {
            // Add a new robot in the network if its ID and port are unique
            Position position = this.rn.addRobot(robot);
            List<RobotInfo> robotsList = this.rn.getRobotsList();
            System.out.println("Current robots list:");
            rn.printList();

            // If the insertion succeed, return required info to the robot
            return Response.ok(new AddRobotMessage(position, robotsList), MediaType.APPLICATION_JSON).build();
        } catch (RobotIDAlreadyPresentException e) {
            System.out.println("Rejected robot with id = " + robot.getId() + " because the same id is yet in use.");
            return Response.status(Response.Status.CONFLICT).entity(e).build();
        }catch (RobotPortAlreadyPresentException e) {
            System.out.println("Rejected robot with port = " + robot.getPort() + " because the same port is yet in use.");
            return Response.status(Response.Status.CONFLICT).entity(e).build();
        }
    }

    // Delete a robot by its id
    @Path("delete/{id}")
    @DELETE
    public Response deleteRobot(@PathParam("id") String id){
        try {
            RobotsNetwork.getInstance().deleteRobot(id);
            System.out.println("Deleted robot: " + id);
            System.out.println("Current robots list:");
            rn.printList();
            return Response.ok().build();
        } catch (RobotNotPresentException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
    // Update robot district
    @Path("delete/{id}/{district}")
    @PUT
    public Response updateDistrict(@PathParam("id") String id, @PathParam("district") String district){
        try {
            RobotsNetwork.getInstance().updateDistrict(id, district);
            System.out.println("Updated distric for robot with id = " + id);
            System.out.println("Current robots list:");
            rn.printList();
            return Response.ok().build();
        } catch (RobotNotPresentException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
