package com.pim.server.message;

import com.alibaba.fastjson.JSON;
import com.pim.server.beans.MessageBody;
import com.pim.server.constants.CommParameters;
import com.pim.server.events.*;
import com.pim.server.netty.PrivateChannelSupervise;
import com.pim.server.utils.EncryptionDecryptionUtils;
import com.pim.server.utils.TimeUtils;
import io.netty.channel.Channel;

public class MessageService extends Thread{

    public String clientMessage = "";
    public Channel channel = null;

    @Override
    public void run() {
        super.run();
        doSomeThing();
    }

    private void doSomeThing(){

        String fromUid = "";

        if(clientMessage.indexOf("{") == -1){
            //Cipher text, needs to be decrypted
            fromUid = PrivateChannelSupervise.getUserId(channel);
            String key = EncryptionDecryptionUtils.getUidKey(fromUid);
            clientMessage = EncryptionDecryptionUtils.decrypt(key,clientMessage);
        }


        //Pretend to be an object
        MessageBody messageBody = JSON.parseObject(clientMessage, MessageBody.class);
        //set server receive time
        messageBody.setSTimest(TimeUtils.getTimeSt());

        String eventId = messageBody.getEventId();


        switch (eventId) {
            case "1000000"://Login
                E1000000.login(messageBody,channel);
                break;
            case "1000001"://Send Message to Client
                E1000001.sendMessageToClient(messageBody);
                break;
            case "1000002"://Client receive message after return ack
                break;
            case "1000003"://delete offline
                E1000003.deleteOffline(messageBody,channel);
                break;
            case "1000004"://kick offline
                E1000004.kickOffline(messageBody,channel);
                break;
            case "1000005"://validate token
                E1000005.validateToken(messageBody,channel);
                break;
            case "4000000"://Client get offline message
                E4000000.sendOffLineMessage(messageBody,channel);
                break;
            case "5000000"://create group
                E5000000.createGroup(messageBody,channel);
                break;
            case "5000001"://join group
                E5000001.joinGroup(messageBody,channel);
                break;
            case "5000002"://leave group
                E5000002.quitGroup(messageBody,channel);
                break;
            case "5000003"://disband group
                E5000003.disbandGroup(messageBody,channel);
                break;
            case "5000004"://Send group message
                E5000004.sendMessageToGroup(messageBody,channel);
                break;
            case "9000000"://ping
                E9000000.doPing(messageBody,channel);
                break;
            default://非法调用
                break;
        }


    }


}
