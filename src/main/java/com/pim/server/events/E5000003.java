package com.pim.server.events;

import com.pim.server.beans.MessageBody;
import com.pim.server.utils.EncryptionDecryptionUtils;
import com.pim.server.utils.RedisUtils;
import io.netty.channel.Channel;

public class E5000003 {

    public static void disbandGroup(MessageBody messageBody, Channel channel) {

        String groupId = messageBody.getGroupId();
        String fromUid = messageBody.getFromUid();
        String key = EncryptionDecryptionUtils.getUidKey(fromUid);

        if (groupId.trim().length() == 0) {
            String rs = CommEvent.createActionReturn("Failed to disband group,groupId is null.", "ERROR",messageBody.getCTimest(),messageBody.getEventId());
            rs = EncryptionDecryptionUtils.encrypt(key, rs);
            CommEvent.wirteToclient(rs,channel);
            return;
        }

        if(RedisUtils.instance().getRedissonClient().getMap("group_list").containsKey(groupId)){
            RedisUtils.instance().getRedissonClient().getMap("group_list").remove(groupId);
        }

        if(RedisUtils.instance().getRedissonClient().getMap(groupId).isExists()){
            RedisUtils.instance().getRedissonClient().getMap(groupId).delete();
        }

        String rs = CommEvent.createActionReturn("Group disband successfully", "OK",messageBody.getCTimest(),messageBody.getEventId());
        rs = EncryptionDecryptionUtils.encrypt(key, rs);
        CommEvent.wirteToclient(rs,channel);


    }
}
