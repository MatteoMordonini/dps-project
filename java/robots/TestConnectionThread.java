package robots;

import administrators.RobotInfo;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import proto.wmessage.RemoveRobotMessage;
import proto.wmessage.RobotRPCGrpc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestConnectionThread extends Thread{
    @Override
    public void run() {
        RobotDataManager rdm = RobotDataManager.getInstance();
        while (true){
            // Every 10 seconds check if every robot is reachable
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            List<RobotInfo> tempRobotsList;
            synchronized (rdm.getRobotsListLock()){
                tempRobotsList = new ArrayList<>(rdm.getRobotDynamicData().getRobotInitDataList());
            }
            for (RobotInfo r : tempRobotsList){
                new Thread(() -> {
                    final ManagedChannel channel = ManagedChannelBuilder.forTarget(r.getRobotInitData().getAddress() + r.getRobotInitData().getPort()).usePlaintext().build();
                    RobotRPCGrpc.RobotRPCStub stub = RobotRPCGrpc.newStub(channel);
                    stub.testConnection(Empty.newBuilder().build(),new StreamObserver<Empty>() {
                        @Override
                        public void onNext(Empty value) {

                        }

                        @Override
                        public void onError(Throwable t) {
                            // Communicate to other robots that this robot left the network in an uncontrolled way
                            int leftRobotId = r.getRobotInitData().getId();
                            System.out.println("Alert: Robot with id = " + leftRobotId + " left the network (crashing).");

                            System.out.println("Telling other robots that robot with id = " + leftRobotId + " left the network.");
                            List<RobotInfo> tempRobotsList;
                            synchronized (rdm.getRobotsListLock()){
                                tempRobotsList = new ArrayList<>(rdm.getRobotDynamicData().getRobotInitDataList());
                            }
                            for (RobotInfo r : tempRobotsList){
                                new Thread(() -> {
                                    final ManagedChannel channel = ManagedChannelBuilder.forTarget(r.getRobotInitData().getAddress() + r.getRobotInitData().getPort()).usePlaintext().build();
                                    RobotRPCGrpc.RobotRPCStub stub = RobotRPCGrpc.newStub(channel);
                                    stub.robotUWLeft(RemoveRobotMessage.newBuilder().setId(leftRobotId).build(),new StreamObserver<Empty>() {
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

                                }).start();
                            }
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
}
