package robots;

import administrators.RobotInfo;
import administrators.RobotsServices;
import com.google.protobuf.Empty;
import com.sun.jersey.api.client.*;
import constants.Constants;
import exceptions.InvalidStatusFromServerException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import proto.wmessage.RemoveRobotMessage;
import proto.wmessage.RobotRPCGrpc;
import robots.maintenance.TriggerMaintenanceThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class UserInteractionThread extends Thread{

    @Override
    public void run() {
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        boolean exit = false;
        while (!exit) {
            System.out.println("Select an action by its number:");
            System.out.println("1. Quit operation (close the robot program).");
            System.out.println("2. Fix operation (force the maintenance).");
            try{
                String input = userInput.readLine();
                int option = Integer.parseInt(input);
                switch (option){
                    case 1: quitOperation(); exit = true; break;
                    case 2: fixOperation(); break;
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

    private void fixOperation() {
        RobotDataManager rdm = RobotDataManager.getInstance();
        // Trigger maintenance if i'm not waiting on it and i'm not doing it
        if (rdm.isNeedMaintenance() == false && rdm.isInMaintenance() ==false){
            // Maintenance is triggered
            System.out.println("Requested maintenance. Wait for it.");
            rdm.setNeedMaintenance(true);
            TriggerMaintenanceThread triggerMaintenanceThread = new TriggerMaintenanceThread();
            triggerMaintenanceThread.start();
            try {
                triggerMaintenanceThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Impossible operation: The maintenance process had been yet activated.");
        }

    }

    private void quitOperation() {
        RobotDataManager rdm = RobotDataManager.getInstance();
                synchronized (rdm.getQuittingLock()) {
                    String path = Constants.AS_ADDRESS + RobotsServices.ROBOT_SERVICES_PATH + "/delete/" + rdm.getRobotInitData().getId();
                    Client client = Client.create();

                    boolean requestPerformed = false;
                    while(!requestPerformed) {
                        try {
                            WebResource webResource = client.resource(path);
                            ClientResponse clientResponse = webResource.delete(ClientResponse.class);
                            System.out.println("Sended request to the server.");
                            int status = clientResponse.getStatus();
                            if (ClientResponse.Status.OK.getStatusCode() == status) {
                                //Robot correctly deleted from the AS
                                requestPerformed = true;
                            } else {
                                throw new InvalidStatusFromServerException(status);
                            }
                            requestPerformed = true;
                        } catch (ClientHandlerException | UniformInterfaceException | InvalidStatusFromServerException e) {
                            System.out.println("Unable to perform the remove request. Next try in 2 seconds.");
                            System.out.println("Error message: " + e.getMessage());
                        }
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    // Tell other robots that i leave the network

                    synchronized (rdm.getRobotDynamicData().getRobotInitDataList()){
                        for (RobotInfo r : rdm.getRobotDynamicData().getRobotInitDataList()){
                            Thread t = new Thread(() -> {
                                final ManagedChannel channel =
                                        ManagedChannelBuilder.forTarget(r.getRobotInitData().getAddress() + r.getRobotInitData().getPort()).usePlaintext().build();
                                RobotRPCGrpc.RobotRPCStub stub = RobotRPCGrpc.newStub(channel);
                                stub.removeRobot(RemoveRobotMessage.newBuilder().setId(rdm.getRobotInitData().getId()).build(),new StreamObserver<Empty>() {
                                    @Override
                                    public void onNext(Empty value) {

                                    }

                                    @Override
                                    public void onError(Throwable t) {
                                        channel.shutdown();
                                    }

                                    @Override
                                    public void onCompleted() {
                                        channel.shutdown();
                                    }
                                });

                                try {
                                    channel.awaitTermination(1, TimeUnit.SECONDS);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            });
                            t.start();
                            try {
                                // Wait until every robot receives the message
                                t.join();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
    }
}
