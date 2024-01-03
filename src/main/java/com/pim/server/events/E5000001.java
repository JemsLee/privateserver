package com.pim.server.events;

import com.pim.server.beans.MessageBody;
import com.pim.server.utils.EncryptionDecryptionUtils;
import com.pim.server.utils.RedisUtils;
import com.pim.server.utils.TimeUtils;
import io.netty.channel.Channel;

public class E5000001 {

    public static void joinGroup(MessageBody messageBody, Channel channel) {

        String groupId = messageBody.getGroupId();
        String fromUid = messageBody.getFromUid();
        String key = EncryptionDecryptionUtils.getUidKey(fromUid);

        if (groupId.trim().length() == 0) {
            String rs = CommEvent.createActionReturn("Failed to join group,groupId is null.", "ERROR",messageBody.getCTimest(),messageBody.getEventId());
            rs = EncryptionDecryptionUtils.encrypt(key, rs);
            CommEvent.wirteToclient(rs,channel);
            return;
        }

        RedisUtils.instance().getRedissonClient().getMap(groupId).put(fromUid, TimeUtils.getDateTime());

        String rs = CommEvent.createActionReturn("Join Group successfully", "OK",messageBody.getCTimest(),messageBody.getEventId());
        rs = EncryptionDecryptionUtils.encrypt(key, rs);
        CommEvent.wirteToclient(rs,channel);

    }
}
