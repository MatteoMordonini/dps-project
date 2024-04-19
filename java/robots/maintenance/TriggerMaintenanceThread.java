package robots.maintenance;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import proto.wmessage.*;
import robots.MessageCounter;
import robots.RobotDataManager;
import administrators.RobotInfo;
import robots.RobotInitData;

import java.util.concurrent.TimeUnit;

import java.util.ArrayList;
import java.util.List;

// Start the maintenance communication with other robots
public class TriggerMaintenanceThread extends Thread{
    @Override
    public void run() {
        synchronized (RobotDataManager.getInstance().getQuittingLock()){
            System.out.println("Requested maintenance.");

            RobotDataManager rdm = RobotDataManager.getInstance();

            // Maintenance message must be sent to other robots
            rdm.setMaintenanceTs(System.currentTimeMillis());
            //Build message to be sent
            MaintenanceMessage maintenanceMessage = MaintenanceMessage.newBuilder()
                    .setTimestamp(rdm.getMaintenanceTs())
                    .setRobotWelcomeMessage(RobotWelcomeMessage.newBuilder()
                            .setAddress(rdm.getRobotInitData().getAddress())
                            .setPort(rdm.getRobotInitData().getPort())
                            .setId(rdm.getRobotInitData().getId())
                            .setDistrect(rdm.getRobotDynamicData().getRobotDistrict())
                            .setPosition(PositionMessage.newBuilder()
                                    .setX(rdm.getRobotDynamicData().getRobotPosition().getX())
                                    .setY(rdm.getRobotDynamicData().getRobotPosition().getY())
                                    .build())
                            .build())
                    .build();

            ArrayList<RobotInfo> askOkList = new ArrayList<>(rdm.getRobotDynamicData().getRobotInitDataList());
            rdm.setMessageCounter(new MessageCounter(askOkList.size(), askOkList));
            sendRequest(maintenanceMessage, askOkList);
            // Wait until every ok is received
            synchronized (rdm.getMessageCounter().getLock()) {
                while (rdm.getMessageCounter().getCurrentResponses() < rdm.getMessageCounter().getLimit()) {
                    try {
                        rdm.getMessageCounter().getLock().wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            // When receiving all the OK, go to sleep for 10 seconds and then end maintenance
            System.out.println("Started the maintenance.");
            rdm.setInMaintenance(true);
            rdm.setNeedMaintenance(false);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Ended maintenance after 10 seconds.");
            rdm.setInMaintenance(false);
            System.out.println("Sending OK messages to every waiting robot.");
            sendOk();
        }

    }

    private void sendOk() {
        RobotDataManager rdm = RobotDataManager.getInstance();
        List<RobotInitData> robotsList = rdm.getAllAndClean();
        OkMessage okMessage = OkMessage.newBuilder().setOk("OK").build();
        for (RobotInitData r : robotsList){
            new Thread(() -> {
                final ManagedChannel channel = ManagedChannelBuilder.forTarget(r.getAddress()+ r.getPort()).usePlaintext().build();
                RobotRPCGrpc.RobotRPCStub stub = RobotRPCGrpc.newStub(channel);

                stub.sendOkMessage(okMessage, new StreamObserver<Empty>() {
                            @Override
                            public void onNext(Empty value) {

                            }

                            @Override
                            public void onError(Throwable t) {
                                System.out.println("Error sending OK message: " + t.getMessage());
                                channel.shutdown();
                            }

                            @Override
                            public void onCompleted() {
                                channel.shutdown();
                            }
                        }
                );
                try {
                    channel.awaitTermination(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    System.out.println("Error occurred waiting for termination of sending OK message: " + e.getMessage());
                }
            }).start();
        }
    }

    private void sendRequest(MaintenanceMessage maintenanceMessage, ArrayList<RobotInfo> askOkList) {
        for (RobotInfo r : askOkList){
            new Thread(() -> {
                final ManagedChannel channel = ManagedChannelBuilder.forTarget(r.getRobotInitData().getAddress() + r.getRobotInitData().getPort()).usePlaintext().build();
                RobotRPCGrpc.RobotRPCStub stub = RobotRPCGrpc.newStub(channel);
                stub.reqMaintenance(maintenanceMessage, new StreamObserver<OkMessage>() {
                    @Override
                    public void onNext(OkMessage response) {
                        if (response.getOk().equals("OK")) {
                            RobotDataManager.getInstance().getMessageCounter().incrCounter();
                        }
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
                    channel.awaitTermination(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }).start();
        }
    }
}
