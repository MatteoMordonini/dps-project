package robots;
import administrators.RobotInfo;
import administrators.AddRobotMessage;
import com.google.protobuf.Empty;
import exceptions.*;
import greenfield.Position;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerBuilder;
import proto.wmessage.PositionMessage;
import proto.wmessage.RobotRPCGrpc;
import proto.wmessage.RobotWelcomeMessage;
import com.google.gson.Gson;
import com.sun.jersey.api.client.*;
import constants.Constants;
import robots.maintenance.RandomTriggerMaintenanceThread;
import robots.mqtt.MqttRobotMng;
import administrators.RobotsServices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.sun.jersey.api.client.ClientResponse.Status;
import simulator.BufferImpl;
import simulator.PM10Simulator;

public class Robot {
    private RobotInitData robotInitData;
    private RobotDynamicData robotDynamicData;
    private RobotDataFacility rdFacility;
    private Client restClient;
    private String ASAddress;
    private StatsThread statsThread;
    private PM10Simulator pm10Simulator;
    private MqttRobotMng mqttMng;
    private RobotDataManager robotDataManager;
    public Robot(){
        this.robotInitData = new RobotInitData();
        // Set Administrator Server address
        this.rdFacility = RobotDataFacility.getInstance();
        this.restClient = Client.create();
        this.ASAddress = Constants.AS_ADDRESS;
    }
    public static void main(String[] argv) {
        Robot robot = new Robot();
        try {
            robot.initRobot();
            System.out.println("program is closing");
            System.exit(0);
        } catch (InvalidRobotInitDataException e) {
            System.out.println(e.getMessage());
            // End of program cause invalid ID or PORT
        }
    }


    private void initRobot() throws InvalidRobotInitDataException {
        // Set ID and port
        initRobotId();
        // Request admission to AS
        if(requestAdmission()){
            System.out.println("Accepted by the Administrator Server");
            System.out.println("Current robots list is:");
            this.robotDataManager.printList();

            // start mqtt broker connection
            configMqtt();
            // Start some robot's thread
            startThreads();
            // Welcome Message
            if(!this.robotDynamicData.getRobotInitDataList().isEmpty()){
                sendWelcomeMessage();
            }
            // Start interaction with the user
            UserInteractionThread uit = new UserInteractionThread();
            uit.start();
            try {
                uit.join();
                System.out.println("Closing robot program...");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            System.out.println("Request to admission rejected by the AS.");
        }
    }

    // Config and connect to mqtt broker
    private void configMqtt() {
        // Robot id = mqtt client id
        System.out.println(this.robotInitData.getId() + " Connecting Broker " + Constants.MQTT_BROKER_ADDRESS);
        this.mqttMng = MqttRobotMng.getInstance();
        this.mqttMng.clientConfig(Constants.MQTT_BROKER_ADDRESS, Integer.toString(this.robotInitData.getId()));
        System.out.println(this.robotInitData.getId() + " Connected");
    }

    private void startThreads() {
        startGRPCThread();
        BufferImpl buffer = new BufferImpl();
        statsThread = new StatsThread(buffer, this.robotInitData, this.robotDynamicData);
        statsThread.start();
        pm10Simulator = new PM10Simulator(buffer);
        pm10Simulator.start();
        RandomTriggerMaintenanceThread rtmt = new RandomTriggerMaintenanceThread();
        rtmt.start();
        TestConnectionThread tct = new TestConnectionThread();
        tct.start();
    }

    private void startGRPCThread() {
        Thread grpcThread = new Thread(() -> {
            try {
                io.grpc.Server rpcServer = ServerBuilder.forPort(this.robotInitData.getPort())
                        .addService(new RobotRPCImpl()).build();
                rpcServer.start();
                rpcServer.awaitTermination();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        grpcThread.start();
    }

    private void sendWelcomeMessage() {
        // Message building
        RobotWelcomeMessage robotWelcomeMessage = RobotWelcomeMessage.newBuilder().
                setId(this.robotInitData.getId()).
                setDistrect(this.rdFacility.getDistrict(this.robotDynamicData.getRobotPosition())).
                setPort(this.robotInitData.getPort()).
                setAddress(this.robotInitData.getAddress()).
                setPosition(PositionMessage.newBuilder().
                        setX(this.robotDynamicData.getRobotPosition().getX()).
                        setY(this.robotDynamicData.getRobotPosition().getY()).build()).
                build();
        // Sending message to each robot in the list
        for(RobotInfo robot : this.robotDynamicData.getRobotInitDataList()){
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(robot.getRobotInitData().getAddress()+ robot.getRobotInitData().getPort()).usePlaintext().build();
            RobotRPCGrpc.RobotRPCBlockingStub stub = RobotRPCGrpc.newBlockingStub(channel);
            Empty result = stub.addRobot(robotWelcomeMessage);
            channel.shutdown();
        }
    }

    private boolean requestAdmission() throws InvalidRobotInitDataException {

        String pathRequest = Constants.AS_ADDRESS + RobotsServices.ROBOT_SERVICES_PATH + "/add";
        WebResource webResource = restClient.resource(pathRequest);
        String requestInput = new Gson().toJson(this.robotInitData);

        boolean requestPerformed = false;
        while(!requestPerformed) {
            try {
                ClientResponse clientResponse = webResource.type("application/json").post(ClientResponse.class, requestInput);
                int status = clientResponse.getStatus();
                if (Status.OK.getStatusCode() == status) {
                    //Robot correctly added
                    // Get other robots data
                    AddRobotMessage addRobotMessage = clientResponse.getEntity(AddRobotMessage.class);
                    Position position = addRobotMessage.getPosition();
                    List<RobotInfo> robotsList = addRobotMessage.getRobotsList();

                    this.robotDynamicData = new RobotDynamicData(position, robotsList, this.rdFacility.getDistrict(position));
                    this.robotDataManager = RobotDataManager.getInstance();
                    this.robotDataManager.setRobotDynamicData(this.robotDynamicData);
                    this.robotDataManager.setRobotInitData(this.robotInitData);
                } else if (Status.CONFLICT.getStatusCode() == status) {
                    //Robot id or port already added
                    throw new InvalidRobotInitDataException();
                } else {
                    throw new InvalidStatusFromServerException(status);
                }
                requestPerformed = true;
            } catch (ClientHandlerException | UniformInterfaceException | InvalidStatusFromServerException e) {
                // In case of network problems
                System.out.println("Unable to perform the request admission. Next try in 2 seconds.");
                System.out.println("Error message: " + e.getMessage());
                waitBeforeNewRequest(2000);

            }
        }
        return true;
    }
    private void waitBeforeNewRequest(int mil){
        try {
            Thread.sleep(mil);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initRobotId() {
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        while(this.robotInitData.getId() == -1 || this.robotInitData.getPort() == -1){
            System.out.println("Robot initialization:");
            System.out.println("- ID: Insert ad ID (0-99) or write \"r\" to assign a random one");
            try {
                String idFromUser = bReader.readLine();
                int id = parseIdFromUser(idFromUser);
                this.robotInitData.setId(id);
                System.out.println("Insert a port:");
                idFromUser = bReader.readLine();
                robotInitData.setPort(Integer.parseInt(idFromUser));
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch(NumberFormatException e){
                System.out.println("Error: Invalid option");
            }
        }
    }

    private int parseIdFromUser(String id) throws NumberFormatException{
        if(id.equals("r")){
            // Assign a random id
            return this.rdFacility.getRandomID();
        } else{
                int intId = Integer.parseInt(id);
                if (intId >= Constants.MIN_ROBOT_ID && intId <= Constants.MAX_ROBOT_ID){
                    // Assign id chosen by the user
                    return intId;
                }else {
                    throw new NumberFormatException();
                }
        }

    }
}
