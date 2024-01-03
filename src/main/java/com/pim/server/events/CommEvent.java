package com.pim.server.events;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pim.server.beans.ActionReturnBody;
import com.pim.server.beans.PublishMessageBody;
import com.pim.server.beans.PublishOffLineBody;
import com.pim.server.constants.CommParameters;
import com.pim.server.message.MessageService;
import com.pim.server.netty.PrivateChannelSupervise;
import com.pim.server.utils.RedisUtils;
import com.pim.server.utils.TimeUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.redisson.api.RTopic;
import org.redisson.codec.SerializationCodec;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class CommEvent {

    /**
     * 创建ACK消息体
     *
     * @param desc
     * @param status
     * @return
     */
    public static String createActionReturn(String desc, String status, String cTimes, String eventId) {
        ActionReturnBody actionReturnBody = new ActionReturnBody();
        actionReturnBody.setEventId("000000");
        actionReturnBody.setResDesc(desc);
        actionReturnBody.setSTimest(TimeUtils.getTimeSt());
        actionReturnBody.setEStatus(status);
        actionReturnBody.setCTimest(cTimes);
        actionReturnBody.setInEventId(eventId);
        JSONObject jsonObject = (JSONObject) JSON.toJSON(actionReturnBody);
        return jsonObject.toJSONString();
    }

    public static void wirteToclient(String rs,Channel channel){
        TextWebSocketFrame tws = new TextWebSocketFrame(rs);
        channel.writeAndFlush(tws);
    }


    public static boolean checkUserOnlione(String fromUid){
        Channel channel = PrivateChannelSupervise.getChannelByUserId(fromUid);
        if(channel != null){
            String rs = CommEvent.createActionReturn("kicked offline", "OK", TimeUtils.getTimeSt(), "1000004");
            wirteToclient(rs,channel);
            channel.disconnect();
            clearUserInfo(fromUid);
        }else {
            //The link is on another server and needs to be notified to go offline.
            return checkUserOnOtherServer(fromUid);
        }
        return false;
    }

    public static void clearUserInfo(String fromUid){
        ConcurrentMap<String, String> userInfoCache = RedisUtils.instance().getRedissonClient().getMap(fromUid + "_online");
        String serverIp = userInfoCache.get("serverIp");
        String serverPort = userInfoCache.get("serverPort");
        if(CommParameters.instance().getServerIp().indexOf(serverIp) >= 0
        && Integer.parseInt(serverPort) == CommParameters.instance().getServerPort()){
            RedisUtils.instance().getRedissonClient().getMap(fromUid + "_online").delete();
        }
    }

    public static void clearLocalUserInfo(String fromUid){
        if(RedisUtils.instance().getRedissonClient().getMap(fromUid + "_online").isExists()){
            RedisUtils.instance().getRedissonClient().getMap(fromUid + "_online").delete();
            PrivateChannelSupervise.offline(fromUid);
        }
    }

    /**
     * Clear client temporary login data, only clear clients linked to this server, data source: data bound at login
     */
    public static void clearAllUserInfo(){

        for (Map.Entry entry : CommParameters.instance().getOnlineUser().entrySet()) {
            String fromUid = entry.getKey().toString();
            Channel channel = PrivateChannelSupervise.getChannelByUserId(fromUid);
            if(channel != null){
                channel.disconnect();
                PrivateChannelSupervise.offline(fromUid);
            }
            RedisUtils.instance().getRedissonClient().getMap(fromUid + "_online").delete();
        }
    }

    public static boolean checkUserOnOtherServer(String fromUid){
        if(RedisUtils.instance().getRedissonClient().getMap(fromUid + "_online").isExists()){
            ConcurrentMap<String, String> userInfoCache = RedisUtils.instance().getRedissonClient().getMap(fromUid + "_online");
            String serverIp = userInfoCache.get("serverIp");
            String serverPort = userInfoCache.get("serverPort");
            String publishKey = serverIp + "_" + serverPort + "_publish";
            RTopic rTopic = RedisUtils.instance().getRedissonClient().getTopic(publishKey, new SerializationCodec());
            PublishOffLineBody publishOffLineBody = new PublishOffLineBody();
            publishOffLineBody.setFromUid(fromUid);
            rTopic.publish(publishOffLineBody);
            return true;
        }
        return false;
    }



    /**
     * This is a notification received from other servers and the user is kicked offline.
     */
    public static void setPublish(){

        String serverIp = CommParameters.instance().getServerIp();
        int serverPort = CommParameters.instance().getServerPort();
        String publishKey = serverIp + "_" + serverPort + "_publish";

        RTopic userStatus = RedisUtils.instance().getRedissonClient().getTopic(publishKey,new SerializationCodec());
        userStatus.addListener(PublishOffLineBody.class, (charSequence, publishOffLineBody) -> {
            try {
                String fromUid = publishOffLineBody.getFromUid();
                CommEvent.clearUserInfo(fromUid);
                String rs = CommEvent.createActionReturn("kicked offline", "OK", TimeUtils.getTimeSt(), "1000004");
                Channel channel = PrivateChannelSupervise.getChannelByUserId(fromUid);
                CommEvent.wirteToclient(rs, channel);
                Thread.sleep(500);
                channel.disconnect();
            }catch (Exception exception){
                exception.printStackTrace();
            }

        });

        String publishMessageKey = serverIp + "_" + serverPort + "_message";
        RTopic messageTran = RedisUtils.instance().getRedissonClient().getTopic(publishMessageKey,new SerializationCodec());
        messageTran.addListener(PublishMessageBody.class, (charSequence, publishMessageBody) -> {
            try {

                String toUid = publishMessageBody.getToUid();
                Channel channel = PrivateChannelSupervise.getChannelByUserId(toUid);
                if(channel != null){
                    MessageService messageService = new MessageService();
                    messageService.clientMessage = publishMessageBody.getMessage();
                    messageService.channel = channel;
                    CommParameters.instance().getExecutor().execute(messageService);
                }

            }catch (Exception exception){
                exception.printStackTrace();
            }

        });

    }



}
