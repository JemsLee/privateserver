package com.pim.server.events;

import com.pim.server.beans.MessageBody;
import io.netty.channel.Channel;

public class E1000004 {

    /**
     * Kick Offline
     */
    public static void kickOffline(MessageBody messageBody,Channel channel ) {

        String toUid = messageBody.getToUid();
        CommEvent.checkUserOnlione(toUid);

    }
}
