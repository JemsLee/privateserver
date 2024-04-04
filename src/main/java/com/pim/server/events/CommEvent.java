package com.pim.server.events;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pim.server.beans.ActionReturnBody;
import com.pim.server.beans.PublishMessageBody;
import com.pim.server.beans.PublishOffLineBody;
import com.pim.server.client.PriImClient;
import com.pim.server.constants.CommParameters;
import com.pim.server.message.MessageService;
import com.pim.server.netty.PrivateChannelSupervise;
import com.pim.server.utils.EncryptionDecryptionUtils;
import com.pim.server.utils.RedisUtils;
import com.pim.server.utils.TimeUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.codec.SerializationCodec;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        if(channel != null) {
            TextWebSocketFrame tws = new TextWebSocketFrame(rs);
            channel.writeAndFlush(tws);
        }
    }


    public static boolean checkUserOnlione(String fromUid){
        boolean isOnline = false;
        Channel channel = PrivateChannelSupervise.getChannelByUserId(fromUid);
        if(channel != null){
            String rs = CommEvent.createActionReturn("kicked offline", "OK", TimeUtils.getTimeSt(), "3000001");
            wirteToclient(rs,channel);
            channel.disconnect();
            clearUserInfo(fromUid);
            isOnline = true;
        }else {
            //The link is on another server and needs to be notified to go offline.
            return checkUserOnOtherServer(fromUid);
        }
        return isOnline;
    }

    public static void clearUserInfo(String fromUid){
        ConcurrentMap<String, String> userInfoCache = RedisUtils.instance().getRedissonClient().getMap(fromUid + "_online");
        String serverIp = userInfoCache.get("serverIp");
        String serverPort = userInfoCache.get("serverPort");
        if(serverIp != null && serverPort != null)
        if(CommParameters.instance().getServerIp().indexOf(serverIp) >= 0
        && Integer.parseInt(serverPort) == CommParameters.instance().getServerPort()){
            RedisUtils.instance().getRedissonClient().getMap(fromUid + "_online").delete();
        }
    }

    public static void clearLocalUserInfo(String fromUid){
        //clear user ID locally
        CommParameters.instance().getOnlineUser().remove(fromUid);

        //clear redis cache
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
                String rs = CommEvent.createActionReturn("kicked offline", "OK", TimeUtils.getTimeSt(), "3000001");
                Channel channel = PrivateChannelSupervise.getChannelByUserId(fromUid);
                CommEvent.wirteToclient(rs, channel);
                Thread.sleep(500);
                channel.disconnect();
                CommEvent.clearUserInfo(fromUid);
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


    public static void closeDbConn(Connection conn, PreparedStatement ptmt, ResultSet rs) {
        try {
            if (rs != null)
                rs.close();
            if (ptmt != null)
                ptmt.close();
            if (conn != null)
                conn.close();
        } catch (Exception e) {
        }
    }


    public static void sendOfflineMessage(String toUid,Channel channel){

        String offlineKey = toUid + "_offline";
        RMap<String,String>  rMap = RedisUtils.instance().getRedissonClient().getMap(offlineKey);
        Map<String,String> temp = rMap.readAllMap();
        String key = EncryptionDecryptionUtils.getUidKey(toUid);
        temp.forEach((vKey,value)->{
            String rs = EncryptionDecryptionUtils.encrypt(key, value);
            CommEvent.wirteToclient(rs,channel);
        });
        rMap.clear();

    }


    public static void setServerStatus(String type){

        String serverkey = CommParameters.instance().getServerIp() + "_" + CommParameters.instance().getServerPort();
        if(type.contains("up")){
            RedisUtils.instance().getRedissonClient().getMap("imserver_cluster").put(serverkey,TimeUtils.getDateTime());
        }else if(type.contains("down")){
            RedisUtils.instance().getRedissonClient().getMap("imserver_cluster").remove(serverkey);
        }

    }

    public static void connectToOtherServer(){

        String serverKey = "imserver_cluster";
        RMap<String,String>  rMap = RedisUtils.instance().getRedissonClient().getMap(serverKey);
        Map<String,String> temp = rMap.readAllMap();
        temp.forEach((vKey,value)->{
            String[] arr = vKey.split("_");
            String serverIp = arr[0];
            int port = Integer.parseInt(arr[1]);

            if(serverIp.equals(CommParameters.instance().getServerIp()) && port == CommParameters.instance().getServerPort() ){
            }else {

                String server = "ws://"+serverIp+":"+port;

                if (!CommParameters.instance().getOnlineServer().containsKey(server)) {
                    PriImClient priImClient = new PriImClient();
                    priImClient.serverIp = server;
                    priImClient.fromUid = CommParameters.instance().getImUser();
                    priImClient.init();
                    CommParameters.instance().getOnlineServer().put(server,priImClient);
                }


            }


        });


    }


    public static void sendToOtherServerThrowRedisPublish(String publishKey,String toUid,String json){
        RTopic rTopic = RedisUtils.instance().getRedissonClient().getTopic(publishKey, new SerializationCodec());
        PublishMessageBody publishMessageBody = new PublishMessageBody();
        publishMessageBody.setToUid(toUid);
        publishMessageBody.setMessage(json);
        rTopic.publish(publishMessageBody);
    }



}
