package com.pim.server.schedul;

import com.pim.server.constants.CommParameters;
import com.pim.server.events.CommEvent;
import com.pim.server.utils.TimeUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SchedulTasker {

    @Scheduled(cron = "0/3 * * * * ?")
    @Async
    public void checkOtherServer() {
        if (CommParameters.instance().isRedisIsOk()) {
            if(!CommParameters.instance().isConnectingOtherServer()) {
                CommParameters.instance().setConnectingOtherServer(true);
                CommEvent.connectToOtherServer();
                CommParameters.instance().setConnectingOtherServer(false);
            }
        }
    }


    @Scheduled(cron = "0/59 * * * * ?")
    @Async
    public void handleGC() {
        System.out.println("exec gc:"+ TimeUtils.getDateTime());
        System.gc();
    }

}