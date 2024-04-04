package com.pim.server.events;

import com.pim.server.beans.MessageBody;
import com.pim.server.utils.EncryptionDecryptionUtils;
import com.pim.server.utils.RedisUtils;
import io.netty.channel.Channel;
import org.redisson.api.RMap;

import java.util.Map;

public class E5000004 {

    public static void sendMessageToGroup(MessageBody messageBody, Channel channel) {


        //Save chat message for web
        ChatMessageService.save(messageBody);

        String groupId = messageBody.getGroupId();
        String fromUid = messageBody.getFromUid();
        String key = EncryptionDecryptionUtils.getUidKey(fromUid);

        if (groupId.trim().length() == 0) {
            String rs = CommEvent.createActionReturn("Failed to send message to group,groupId is null.", "ERROR",messageBody.getCTimest(),messageBody.getEventId());
            rs = EncryptionDecryptionUtils.encrypt(key, rs);
            CommEvent.wirteToclient(rs,channel);
            return;
        }

        RMap<String, String> cacheMap = RedisUtils.instance().getRedissonClient().getMap(groupId);

        Map<String,String> cacheM = cacheMap.readAllMap();

        messageBody.setEventId("1000001");
        messageBody.setIsCache("0");

        cacheM.forEach((toUid,value)->{
            try {
                if(toUid.trim().length() > 0) {
                    messageBody.setToUid(toUid);
                    E1000001.sendMessageToClient(messageBody);
                }
            }catch (Exception exception){
                exception.printStackTrace();
            }
        });

        String rs = CommEvent.createActionReturn("send message to group successfully", "OK",messageBody.getCTimest(),messageBody.getEventId());
        rs = EncryptionDecryptionUtils.encrypt(key, rs);
        CommEvent.wirteToclient(rs,channel);


    }
}
