package com.pim.server.events;

import com.alibaba.fastjson.JSONObject;
import com.pim.server.beans.MessageBody;
import com.pim.server.constants.CommParameters;
import com.pim.server.utils.RedisUtils;
import com.pim.server.utils.TimeUtils;
import io.netty.channel.Channel;

public class E9000000 {


    public static void doPing(MessageBody messageBody, Channel channel){
        String fromUid = messageBody.getFromUid();
        if(validate(fromUid,channel)) {
            CommParameters.instance().getOnlineUser().put(fromUid, TimeUtils.getTimeSt());
            String rs = CommEvent.createActionReturn("ping ok", "OK", messageBody.getCTimest(), messageBody.getEventId());
            CommEvent.wirteToclient(rs, channel);
        }
    }



    private static boolean validate(String fromUid, Channel channel){

        boolean rs = true;
        try {
            if(!RedisUtils.instance().getRedissonClient().getMap(fromUid + "_online").isExists()){
                channel.disconnect();
                rs = false;
            }else {
                String chanelId = RedisUtils.instance().getRedissonClient().getMap(fromUid + "_online").get("channelId").toString();
                if(!chanelId.equals(channel.id().asLongText())){
                    rs = false;
                    CommEvent.checkUserOnlione(fromUid);
                }
            }

        }catch (Exception e){
            channel.disconnect();
            rs = false;
        }
        return rs;
    }

}
