package com.pim.server.events;

import com.alibaba.fastjson.JSONObject;
import com.pim.server.beans.MessageBody;
import com.pim.server.beans.PublishMessageBody;
import com.pim.server.constants.CommParameters;
import com.pim.server.dbser.ChatMessageService;
import com.pim.server.netty.PrivateChannelSupervise;
import com.pim.server.utils.EncryptionDecryptionUtils;
import com.pim.server.utils.RedisUtils;
import io.netty.channel.Channel;
import org.redisson.api.RTopic;
import org.redisson.codec.SerializationCodec;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentMap;

public class E1000001 {

    /**
     * Send message to client
     */
    public static void sendMessageToClient(MessageBody messageBody) {

        String toUid = messageBody.getToUid();

        JSONObject json = (JSONObject) JSONObject.toJSON(messageBody);

        //Save chat message for web
        ChatMessageService.save(messageBody);


        Channel channel = PrivateChannelSupervise.getChannelByUserId(toUid);

        if (channel != null) {

            String key = EncryptionDecryptionUtils.getUidKey(toUid);
            String rs = EncryptionDecryptionUtils.encrypt(key, json.toJSONString());
            CommEvent.wirteToclient(rs,channel);

        } else {//Not on this machine

            if(RedisUtils.instance().getRedissonClient().getMap(toUid + "_online").isExists()){

                ConcurrentMap<String, String> userInfoCache = RedisUtils.instance().getRedissonClient().getMap(toUid + "_online");
                String serverIp = userInfoCache.get("serverIp");
                String serverPort = userInfoCache.get("serverPort");

                String publishKey = serverIp + "_" + serverPort + "_message";

                RTopic rTopic = RedisUtils.instance().getRedissonClient().getTopic(publishKey, new SerializationCodec());
                PublishMessageBody publishMessageBody = new PublishMessageBody();
                publishMessageBody.setToUid(toUid);
                publishMessageBody.setMessage(json.toJSONString());
                rTopic.publish(publishMessageBody);

            }else {

                if(Integer.parseInt(messageBody.getIsCache()) == 1){
                    LinkedList<String> linkedList = CommParameters.instance().getTempOfflineMessage().get(toUid);
                    if(linkedList == null){
                        linkedList = new LinkedList<>();
                    }
                    linkedList.addFirst(json.toJSONString());
                    CommParameters.instance().getTempOfflineMessage().put(toUid,linkedList);
                }

            }
        }
    }
}
