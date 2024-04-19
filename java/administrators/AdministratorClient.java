package administrators;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import constants.Constants;
import data.StatsMessage;
import administrators.RobotsListMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class AdministratorClient {
    private static Client client = Client.create();
    private static BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));;
    public static void main(String[] argv){

        boolean exit = false;
        while (!exit) {
            System.out.println("Select a service by its number:");
            System.out.println("1. The list of cleaning robots currently located in Greenfield.");
            System.out.println("2. The average of the last n air pollution levels captured by a given robot.");
            System.out.println("3. The average of the air pollution levels captured by all the robots over a period of time.");
            System.out.println("4. Close this program.");

            try{
                String input = userInput.readLine();
                int option = Integer.parseInt(input);
                switch (option){
                    case 1: robotsListService(); break;
                    case 2: averageLastNService(); break;
                    case 3: averageAllRobots(); break;
                    case 4: exit = true; break;
                    default: throw new NumberFormatException();
                }

            } catch (IOException e){
                System.out.println("Error occurred reading the input:");
                e.printStackTrace();
            } catch (NumberFormatException e){
                System.out.println("Selected an invalid option.");
            }

        }
    }

    // Option 3
    private static void averageAllRobots() {
        boolean ok = false;
        String ts1="";
        String ts2="";
        long n1;
        long n2;

        while (!ok) {
            System.out.println("Insert two timestamps (integer values) separated by a new line.");
            try {
                ts1 = userInput.readLine();
                ts2 = userInput.readLine();
                n1 = Long.parseLong(ts1);
                n2 = Long.parseLong(ts2);
                ok = true;
            }catch (IOException e){
                System.out.println("Error during parsing the input");
            } catch (NumberFormatException e) {
                System.out.println("Error: One of the two timestamps is not an integer number.");
            }
        }
        String path = Constants.AS_ADDRESS + StatsServices.STATS_SERVICES_PATH + "/getStatsBwTs/" + ts1 + "/" + ts2;
        ClientResponse clientResponse = getRequest(client, path);
        System.out.println("Sended request to the server.");

        int status = clientResponse.getStatus();

        if (ClientResponse.Status.OK.getStatusCode() == status) {
            StatsMessage statsMessage = clientResponse.getEntity(StatsMessage.class);
            System.out.println("The average of the air pollution levels captured by the robots between " + ts1 + " and " + ts2 + " is " + statsMessage.getAverage());
        }else {
            System.out.println("Error during connection with the Server. Status code = " + status);
            System.out.println("Error message: " + clientResponse.getEntity(String.class));
        }
    }

    // Option 2
    private static void averageLastNService() {
        boolean ok = false;
        String inputId="";
        int n = 0;

        while (!ok) {
            System.out.println("Insert robot id (integer value) and the value of \'n\' separated by a new line.");
            try {
                inputId = userInput.readLine();
                n = Integer.parseInt(userInput.readLine());
                int integerInputId = Integer.parseInt(inputId);
                ok = true;
            }catch (IOException e){
                System.out.println("Error during parsing the input");
            } catch (NumberFormatException e) {
                System.out.println("The id is not an integer, insert a correct value.");
            }
        }
        String path = Constants.AS_ADDRESS + StatsServices.STATS_SERVICES_PATH + "/getLastNStatsByRobot/" + inputId + "/" + n;
        ClientResponse clientResponse = getRequest(client, path);

        int status = clientResponse.getStatus();

        if (ClientResponse.Status.OK.getStatusCode() == status) {
            StatsMessage statsMessage = clientResponse.getEntity(StatsMessage.class);
            System.out.println("The average of the last " + n +" air pollution levels captured by the robot with id = " + inputId + " is " + statsMessage.getAverage());

        } else if (ClientResponse.Status.NOT_FOUND.getStatusCode() == status) {
            System.out.println("Error: " + clientResponse.getEntity(String.class));
        } else {
            System.out.println("Error during connection with the Server. Status code = " + status);
        }
    }

    // Option 1
    private static void robotsListService() {
        String path = Constants.AS_ADDRESS + RobotsServices.ROBOT_SERVICES_PATH + "/getRobotsList";
        ClientResponse clientResponse = getRequest(client, path);

        int status = clientResponse.getStatus();

        if (ClientResponse.Status.OK.getStatusCode() == status) {
            System.out.println("Received the list from the AS.");
            List<RobotInfo> robotInfoList = clientResponse.getEntity(RobotsListMessage.class).getRobotsList();
            if (robotInfoList != null){
                System.out.println("List of robots in the network:");
                for (RobotInfo robot : robotInfoList){
                    System.out.println(robot.toString());
                }
            }else
                System.out.println("There are no robots in the network.");

        }else {
            System.out.println("Error during connection with the Server. Status code = " + status);
        }

    }

    public static ClientResponse getRequest(Client client, String url){
        WebResource webResource = client.resource(url);
        try {
            return webResource.type("application/json").get(ClientResponse.class);
        } catch (ClientHandlerException e) {
            e.printStackTrace();
            return null;
        }
    }

}
