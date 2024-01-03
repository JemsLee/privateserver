package com.pim.server.events;

import com.pim.server.beans.MessageBody;
import com.pim.server.utils.EncryptionDecryptionUtils;
import com.pim.server.utils.RedisUtils;
import io.netty.channel.Channel;
import org.redisson.api.RMap;

import java.util.Map;

public class E4000000 {

    public static void sendOffLineMessage(MessageBody messageBody,Channel channel) {

        String fromUid = messageBody.getFromUid();

        RMap<String, String> cacheMap = RedisUtils.instance().getRedissonClient().getMap(fromUid + "_offline");
        for (Map.Entry entry : cacheMap.entrySet()) {
            String rs = entry.getValue().toString();
            String key = EncryptionDecryptionUtils.getUidKey(fromUid);
            rs = EncryptionDecryptionUtils.encrypt(key, rs);
            CommEvent.wirteToclient(rs,channel);
            cacheMap.remove(entry.getKey().toString());
            try {
                Thread.sleep(50);
            } catch (Exception exception) {

            }
        }
    }
}
