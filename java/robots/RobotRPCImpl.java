package robots;

import administrators.RobotInfo;
import administrators.RobotsServices;
import com.google.protobuf.Empty;
import com.sun.jersey.api.client.*;
import constants.Constants;
import exceptions.RobotNotPresentException;
import io.grpc.stub.StreamObserver;
import proto.wmessage.*;

import java.util.List;

public class RobotRPCImpl extends RobotRPCGrpc.RobotRPCImplBase {
    @Override
    // Add the new robot to the robots list
    public void addRobot(RobotWelcomeMessage request, StreamObserver<Empty> responseObserver) {

        // Create new robot
        RobotInfo newRobot = new RobotInfo(new RobotInitData(request.getId(), request.getPort(), request.getAddress()),
                Integer.toString(request.getDistrect()));
        if (newRobot.getRobotInitData().getId() != RobotDataManager.getInstance().getRobotInitData().getId()){
            // New robot is added to the current robot's list
            RobotDataManager.getInstance().insertRobot(newRobot);

            System.out.println("Added a new robot with id = " + newRobot.getRobotInitData().getId() + ".");
            System.out.println("Current robots list is:");
            RobotDataManager.getInstance().printList();

        }

        Empty empty = Empty.newBuilder().build();
        responseObserver.onNext(empty);
        responseObserver.onCompleted();

    }

    // Remove a robot that is quitting the network
    @Override
    public void removeRobot(RemoveRobotMessage request, StreamObserver<Empty> responseObserver) {

        try {
            RobotDataManager.getInstance().removeRobot(request.getId());
            System.out.println("Current robots list is:");
            RobotDataManager.getInstance().printList();

            Empty empty = Empty.newBuilder().build();
            responseObserver.onNext(empty);
        } catch (RobotNotPresentException e) {
            responseObserver.onError(new RobotNotPresentException());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void reqMaintenance(MaintenanceMessage request, StreamObserver<OkMessage> responseObserver) {
        // If i have sent the request,
        // i don't send the robot id because the sender only needs to count the OK message received
        RobotDataManager rdm = RobotDataManager.getInstance();
        if (request.getRobotWelcomeMessage().getId() == rdm.getRobotInitData().getId()){
            responseObserver.onNext(OkMessage.newBuilder().setOk("OK").build());
            responseObserver.onCompleted();
        }else {
                // I'm not in maintenance and i don't need it
                if (rdm.isNeedMaintenance() == false && rdm.isInMaintenance() == false) {
                    responseObserver.onNext(OkMessage.newBuilder().setOk("OK").build());
                    responseObserver.onCompleted();
                } else
                    // I need maintenance and i'm not doing it
                if (rdm.isNeedMaintenance() == true && rdm.isInMaintenance() == false) {
                    if (request.getTimestamp() <= rdm.getMaintenanceTs()) {
                        // My request came after this one
                        responseObserver.onNext(OkMessage.newBuilder().setOk("OK").build());
                        responseObserver.onCompleted();
                    } else {
                        // My request came before this one
                        System.out.println("Robot with id = " + request.getRobotWelcomeMessage().getId() + " needs maintenance but i have more priority.");
                        int id = request.getRobotWelcomeMessage().getId();
                        int port = request.getRobotWelcomeMessage().getPort();
                        String address = request.getRobotWelcomeMessage().getAddress();
                        rdm.addToSendOkList(new RobotInitData(id, port, address));
                    }
                    // I am charging
                } else if (rdm.isInMaintenance() == true) {
                    System.out.println("Robot with id = " + request.getRobotWelcomeMessage().getId() + " needs maintenance but i am in maintenance");
                    int id = request.getRobotWelcomeMessage().getId();
                    int port = request.getRobotWelcomeMessage().getPort();
                    String address = request.getRobotWelcomeMessage().getAddress();
                    rdm.addToSendOkList(new RobotInitData(id, port, address));
                }
        }
    }

    @Override
    public void sendOkMessage(OkMessage request, StreamObserver<Empty> responseObserver) {
        RobotDataManager rdm = RobotDataManager.getInstance();
        rdm.getMessageCounter().incrCounter();
        System.out.println("Received OK message from robot with id = " + request.getId());
        Empty empty = Empty.newBuilder().build();
        responseObserver.onNext(empty);
        responseObserver.onCompleted();
    }

    // When a robot leave the network in an unexpected way (i.e not caused by quit operation)
    @Override
    public void robotUWLeft(RemoveRobotMessage request, StreamObserver<Empty> responseObserver) {
        RobotDataManager rdm = RobotDataManager.getInstance();
        synchronized (rdm.getRobotsListLock()){
            List<RobotInfo> robotInfoList = rdm.getRobotDynamicData().getRobotInitDataList();
            RobotInfo toRemove = new RobotInfo();
            boolean found = false;
            // Remove from the list the robot who left the network
            for (RobotInfo r : robotInfoList){
                if (r.getRobotInitData().getId() == request.getId()){
                    toRemove = r;
                    found = true;
                }
            }
            if (found == true){
                robotInfoList.remove(toRemove);
            }
            /// if it was in the oklist, then incr the counter
            found = false;
            for (RobotInfo r : rdm.getMessageCounter().getAskOkList()){
                if (request.getId() == r.getRobotInitData().getId()){
                    found = true;
                }
            }
            if (found == true){
                rdm.getMessageCounter().incrCounter();
            }

            ///
            // Communicate to the AS that a robot left the network
            RobotCrashedAlertAS(request.getId());
            // I move to another district if i am the one who must do it
            moveRobot(robotInfoList);
        }
    }

    // Communicate to the AS that a robot left the network
    private void RobotCrashedAlertAS(int id) {
        String path = Constants.AS_ADDRESS + RobotsServices.ROBOT_SERVICES_PATH + "/delete/" + id;
        Client client = Client.create();
        try {
            WebResource webResource = client.resource(path);
            ClientResponse clientResponse = webResource.delete(ClientResponse.class);
            System.out.println("Telling the AS that robot with id = " + id + "crashed");
            int status = clientResponse.getStatus();
            if (ClientResponse.Status.OK.getStatusCode() == status) {
                //Robot correctly deleted from the AS
            } else if(ClientResponse.Status.NOT_FOUND.getStatusCode() == status) {
                System.out.println("AS removed robot with id = " + id + " yet.");
            }
        } catch (ClientHandlerException | UniformInterfaceException e) {
            System.out.println("Unable to perform the remove request for connections problems.");
        }
    }

    private void moveRobot(List<RobotInfo> robotInfoList) {
        int[] districts = {0, 0, 0, 0};
        synchronized (robotInfoList){
            // Count the number of robots in each district and find the max id for each district
            int[] maxIDForDistrict = {0, 0, 0, 0};
            for (RobotInfo r : robotInfoList){
                int district = Integer.parseInt(r.getDistrict()) - 1;
                int id = r.getRobotInitData().getId();

                districts[district]++;
                if (id > maxIDForDistrict[district]){
                    maxIDForDistrict[district] = id;
                }
            }
            int max = 0;
            int min = 0;
            for (int i = 1; i < 4; i++){
                if (districts[i] > districts[max]){
                    max = i;
                }
                if (districts[i] < districts[min]){
                    min = i;
                }
            }
            // max and min district for number of robots in it
            // ++ operation because of the mismatch between districts array and district data of robots
            max++;
            min++;
            if (max - min > 1){
                int robotMovingId = maxIDForDistrict[max];
                // robot with the max id move in the fullest district move to the least full district
                for (RobotInfo r : robotInfoList){
                    if (r.getRobotInitData().getId() == robotMovingId){
                        r.setDistrict(Integer.toString(min));
                    }
                }
                // If i am the moved robot, i communicate the move to AS
                if (RobotDataManager.getInstance().getRobotInitData().getId() == robotMovingId){
                    String pathRequest = Constants.AS_ADDRESS
                            + RobotsServices.ROBOT_SERVICES_PATH
                            + "/delete/"
                            + Integer.toString(robotMovingId)
                            + "/" + Integer.toString(max);
                    Client restClient = Client.create();
                    WebResource webResource = restClient.resource(pathRequest);
                    ClientResponse clientResponse = webResource.put(ClientResponse.class);
                    restClient.destroy();
                }
            }
        }
    }

    // Do nothing, just send and empty response
    @Override
    public void testConnection(Empty request, StreamObserver<Empty> responseObserver) {
        Empty empty = Empty.newBuilder().build();
        responseObserver.onNext(empty);
        responseObserver.onCompleted();
    }
}
