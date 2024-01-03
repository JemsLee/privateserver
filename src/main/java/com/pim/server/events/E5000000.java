package com.pim.server.events;

import com.pim.server.beans.MessageBody;
import com.pim.server.utils.EncryptionDecryptionUtils;
import com.pim.server.utils.RedisUtils;
import com.pim.server.utils.TimeUtils;
import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

public class E5000000 {

    public static void createGroup(MessageBody messageBody, Channel channel) {

        String groupId = messageBody.getGroupId();
        String fromUid = messageBody.getFromUid();
        String key = EncryptionDecryptionUtils.getUidKey(fromUid);

        if (groupId.trim().length() == 0) {
            String rs = CommEvent.createActionReturn("Failed to create group,groupId is null.", "ERROR",messageBody.getCTimest(),messageBody.getEventId());
            rs = EncryptionDecryptionUtils.encrypt(key, rs);
            CommEvent.wirteToclient(rs,channel);
            return;
        }

        RedisUtils.instance().getRedissonClient().getMap("group_list").put(groupId, TimeUtils.getDateTime());

        if(RedisUtils.instance().getRedissonClient().getMap(groupId).isExists()){
            RedisUtils.instance().getRedissonClient().getMap(groupId).delete();
        }


        ConcurrentHashMap<String, String> cacheMap = new ConcurrentHashMap<>();


        cacheMap.put(fromUid, TimeUtils.getDateTime());

        String[] userIds = messageBody.getDataBody().split(",");
        for (String userId : userIds) {
            if (userId.trim().length() > 0) {
                cacheMap.put(userId, TimeUtils.getDateTime());
            }
        }

        RedisUtils.instance().getRedissonClient().getMap(groupId).putAll(cacheMap);


        String rs = CommEvent.createActionReturn("Group created successfully", "OK",messageBody.getCTimest(),messageBody.getEventId());
        rs = EncryptionDecryptionUtils.encrypt(key, rs);
        CommEvent.wirteToclient(rs,channel);


    }
}
