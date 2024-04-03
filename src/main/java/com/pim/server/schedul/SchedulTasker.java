package com.pim.server.schedul;

import com.pim.server.constants.CommParameters;
import com.pim.server.events.CommEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SchedulTasker {

    @Scheduled(cron = "0/10 * * * * ?")
    @Async
    public void checkPingTimes() {
        CommParameters.instance().getTempOfflineMessage().forEach((userId,linkedList)->{
            CacheMessageHelper cacheMessageHelper = new CacheMessageHelper();
            cacheMessageHelper.userId = userId;
            cacheMessageHelper.linkedList = linkedList;
            CommParameters.instance().getExecutor().execute(cacheMessageHelper);
        });
    }

    @Scheduled(cron = "0/3 * * * * ?")
    @Async
    public void checkOtherServer() {
        if(CommParameters.instance().isRedisIsOk()) {
            CommEvent.connectToOtherServer();
        }
    }
}
