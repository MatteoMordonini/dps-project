package robots.maintenance;

import robots.RobotDataManager;
import java.util.concurrent.ThreadLocalRandom;

public class RandomTriggerMaintenanceThread extends Thread{
    @Override
    public void run() {
        RobotDataManager rdm = RobotDataManager.getInstance();
        while(true){
            try {
                Thread.sleep(10000);
                int random = ThreadLocalRandom.current().nextInt(1, 11);
                if (random == 1){
                    // If i'm not in queue and i'm not doing the maintenance, then i trigger it
                    if (rdm.isNeedMaintenance() == false && rdm.isInMaintenance() ==false){
                        // Maintenance is triggered
                        rdm.setNeedMaintenance(true);
                        TriggerMaintenanceThread triggerMaintenanceThread = new TriggerMaintenanceThread();
                        triggerMaintenanceThread.start();
                        triggerMaintenanceThread.join();
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }

    }
}
