package com.pim.server.events;

import com.pim.server.beans.MessageBody;
import com.pim.server.constants.CommParameters;
import com.pim.server.utils.TimeUtils;
import io.netty.channel.Channel;

public class E9000000 {


    public static void doPing(MessageBody messageBody, Channel channel){
        String fromUid = messageBody.getFromUid();
        CommParameters.instance().getOnlineUser().put(fromUid, TimeUtils.getTimeSt());

        String rs = CommEvent.createActionReturn("ping ok","OK",messageBody.getCTimest(),messageBody.getEventId());
        CommEvent.wirteToclient(rs,channel);
    }

}
