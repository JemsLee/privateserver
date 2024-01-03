package com.pim.server.events;

import com.pim.server.beans.MessageBody;
import com.pim.server.constants.CommParameters;
import com.pim.server.netty.PrivateChannelSupervise;
import com.pim.server.utils.RedisUtils;
import com.pim.server.utils.TimeUtils;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.codec.StringCodec;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class E1000000 {

    public static void login(MessageBody messageBody, Channel channel){

        try {

            String fromUid = messageBody.getFromUid();

            if (!RedisUtils.instance().getRedissonClient().getBucket("token_list:" + fromUid, new StringCodec()).isExists()) {
                String rs = CommEvent.createActionReturn("Token NULL", "ERROR", messageBody.getCTimest(), messageBody.getEventId());
                log.error(fromUid + "Token NULL");
                CommEvent.wirteToclient(rs, channel);
                return;
            }

            String userToken = RedisUtils.instance().getRedissonClient().getBucket("token_list:" + fromUid, new StringCodec()).get().toString();
            String clientToken = messageBody.getToken();
            if (userToken.indexOf(clientToken) == -1) {
                String rs = CommEvent.createActionReturn("Token Error", "ERROR", messageBody.getCTimest(), messageBody.getEventId());
                log.error(fromUid + "Token Error");
                CommEvent.wirteToclient(rs, channel);
                return;
            }

            //Determine whether the user is connected to this server. If the link is in the status, kick it down.
            boolean isOnline = CommEvent.checkUserOnlione(fromUid);
            if (isOnline) {
                while (RedisUtils.instance().getRedissonClient().getMap(fromUid + "_online").isExists()) {
                    Thread.sleep(1000);
                }
            }

            //The user goes online and saves the communication channel
            PrivateChannelSupervise.online(channel, fromUid);

            //Cache basic user information to Redis
            ConcurrentMap<String, String> userInfoCache = new ConcurrentHashMap<>();
            userInfoCache.put("userId", fromUid);
            userInfoCache.put("loginTime", TimeUtils.getDateTime());
            userInfoCache.put("serverIp", CommParameters.instance().getServerIp());
            userInfoCache.put("serverPort", CommParameters.instance().getServerPort() + "");
            RedisUtils.instance().getRedissonClient().getMap(fromUid + "_online").putAll(userInfoCache);

            //Cache user ID locally
            CommParameters.instance().getOnlineUser().put(fromUid, TimeUtils.getTimeSt());


            String rs = CommEvent.createActionReturn("Login successful", "OK", messageBody.getCTimest(), messageBody.getEventId());
            CommEvent.wirteToclient(rs, channel);

        }catch (Exception exception){
            exception.printStackTrace();
        }

    }



}
