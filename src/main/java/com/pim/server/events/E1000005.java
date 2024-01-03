package com.pim.server.events;

import com.pim.server.beans.MessageBody;
import com.pim.server.utils.RedisUtils;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.codec.StringCodec;

@Slf4j
public class E1000005 {

    public static void validateToken(MessageBody messageBody, Channel channel){

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

            String rs = CommEvent.createActionReturn("Token OK", "OK", messageBody.getCTimest(), messageBody.getEventId());
            CommEvent.wirteToclient(rs, channel);

        }catch (Exception exception){

        }

    }

}
