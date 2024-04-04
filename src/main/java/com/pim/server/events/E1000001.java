package com.pim.server.events;

import com.alibaba.fastjson.JSONObject;
import com.pim.server.beans.MessageBody;
import com.pim.server.beans.PublishMessageBody;
import com.pim.server.constants.CommParameters;
import com.pim.server.dbser.ChatMessageService;
import com.pim.server.netty.PrivateChannelSupervise;
import com.pim.server.utils.EncryptionDecryptionUtils;
import com.pim.server.utils.RedisUtils;
import com.pim.server.utils.TimeUtils;
import io.netty.channel.Channel;
import org.redisson.api.RTopic;
import org.redisson.codec.SerializationCodec;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class E1000001 {

    /**
     * Send message to client
     */
    public static void sendMessageToClient(MessageBody messageBody) {

        String toUid = messageBody.getToUid();

        JSONObject json = (JSONObject) JSONObject.toJSON(messageBody);

        //Save chat message for web
        //ChatMessageService.save(messageBody);


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

                if(CommParameters.instance().getTransitType() == 0) {
                    //The first mode is through the internal socket.
                    // This mode is suitable for environments with sufficient network bandwidth.
                    String server = "ws://" + serverIp + ":" + serverPort;
                    CommParameters.instance().getOnlineServer().get(server).sendMessageNoEncr(json.toJSONString());
                }else if(CommParameters.instance().getTransitType() == 1) {
                    //The second mode is through Redis publishing and subscription.
                    // This mode is suitable for environments with powerful Redis configuration.
                    String publishKey = serverIp + "_" + serverPort + "_message";
                    CommEvent.sendToOtherServerThrowRedisPublish(publishKey,toUid,json.toJSONString());
                }

            }else {


                //If the user is not online, determine whether offline messages are stored
                if(Integer.parseInt(messageBody.getIsCache()) == 1){
                    try {
                        String offlineKey = toUid + "_offline";
                        int randomNumber = new Random().nextInt(10000);
                        RedisUtils.instance().getRedissonClient().getMap(offlineKey).put(TimeUtils.getNanoTime()+""+randomNumber,json.toJSONString());
                    }catch (Exception e){
                        e.printStackTrace();
                    }


                }

            }
        }
    }
}
