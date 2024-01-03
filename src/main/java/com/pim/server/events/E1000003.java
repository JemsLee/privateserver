package com.pim.server.events;

import com.pim.server.beans.MessageBody;
import com.pim.server.utils.RedisUtils;
import io.netty.channel.Channel;

public class E1000003 {

    /**
     * Delete offline message
     */
    public static void deleteOffline(MessageBody messageBody,Channel channel ) {

        String fromUid = messageBody.getFromUid();
        String offlineKey = fromUid + "_offline";
        int size = 0;
        if(RedisUtils.instance().getRedissonClient().getMap(offlineKey).isExists()) {
            size = RedisUtils.instance().getRedissonClient().getMap(offlineKey).size();
            RedisUtils.instance().getRedissonClient().getMap(offlineKey).delete();
        }

        String rs = CommEvent.createActionReturn(size + " messages deleted", "OK", messageBody.getCTimest(), messageBody.getEventId());
        CommEvent.wirteToclient(rs,channel);

    }
}
