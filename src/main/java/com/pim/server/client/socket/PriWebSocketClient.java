package com.pim.server.client.socket;


import com.alibaba.fastjson.JSONObject;
import com.pim.server.beans.MessageBody;
import com.pim.server.client.observer.PriManagerSubject;
import com.pim.server.utils.EncryptionDecryptionUtils;
import com.pim.server.utils.IMStatus;
import com.pim.server.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PriWebSocketClient extends WebSocketClient {

    public String fromUid = "";
    public String token = "";
    public String deviceId = "";

    public boolean isLogin = false;

    public PriManagerSubject priManagerSubject;


    ScheduledExecutorService connectScheduler;
    ScheduledExecutorService pingScheduler;


    String key;
    String loginString = "";
    String pingString = "";

    public PriWebSocketClient(URI serverUris) {
        super(serverUris);
    }

    @Override
    public void onOpen(ServerHandshake arg0) {
        key = EncryptionDecryptionUtils.getUidKey(fromUid);
        send(loginString);
    }


    @Override
    public void onClose(int arg0, String arg1, boolean arg2) {
        IMStatus imStatus = new IMStatus();
        imStatus.setImcode("501");
        imStatus.setDesc("The server disconnected your connection:" + arg1);
        imStatus.setActionTime(TimeUtils.getDateTime());
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(imStatus);
        priManagerSubject.publishError(jsonObject.toJSONString());
        isLogin = false;
    }

    @Override
    public void onError(Exception arg0) {
        IMStatus imStatus = new IMStatus();
        imStatus.setImcode("502");
        imStatus.setDesc(arg0.getMessage());
        imStatus.setActionTime(TimeUtils.getDateTime());
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(imStatus);
        priManagerSubject.publishError(jsonObject.toJSONString());
        isLogin = false;
    }

    @Override
    public void onMessage(String arg0) {

        if (arg0.indexOf("{") >= 0) {
            if (arg0.indexOf("Login successful") >= 0) {
                isLogin = true;
            }
            priManagerSubject.publish(arg0);
        } else {
            String recStr = EncryptionDecryptionUtils.decrypt(key, arg0);
            priManagerSubject.publish(recStr);
        }
    }

    public void start() {

        loginString = createLoginString();
        pingString = createPingString();
        scheduleReconnect();
        schedulePing();

        this.connect();
    }


    public String createLoginString() {
        MessageBody messageBodyLogin = new MessageBody();
        messageBodyLogin.setEventId("1000000");
        messageBodyLogin.setFromUid(fromUid);
        messageBodyLogin.setToken(token);
        messageBodyLogin.setDeviceId(deviceId);
        messageBodyLogin.setCTimest(TimeUtils.getTimeSt());
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(messageBodyLogin);
        return jsonObject.toJSONString();
    }

    public String createPingString() {
        MessageBody messageBodyLogin = new MessageBody();
        messageBodyLogin.setEventId("9000000");
        messageBodyLogin.setFromUid(fromUid);
        messageBodyLogin.setDeviceId(deviceId);
        messageBodyLogin.setCTimest(TimeUtils.getNanoTime() + "");
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(messageBodyLogin);
        return EncryptionDecryptionUtils.encrypt(EncryptionDecryptionUtils.getUidKey(fromUid), jsonObject.toJSONString());
    }

    private void scheduleReconnect() {
        connectScheduler = Executors.newSingleThreadScheduledExecutor();
        long delay = 10; //Delay execution time (seconds)
        long period = 5; //Execution interval (seconds)
        connectScheduler.scheduleAtFixedRate(this::doReconnect, delay, period, TimeUnit.SECONDS);
    }
    public void stopScheduleReconnect(){
        connectScheduler.shutdownNow();
    }

    private void schedulePing() {
        pingScheduler = Executors.newSingleThreadScheduledExecutor();
        long delay = 3; //Delay execution time (seconds)
        long period = 5; //Execution interval (seconds)
        pingScheduler.scheduleAtFixedRate(this::doPingStr, delay, period, TimeUnit.SECONDS);
    }

    public void stopSchedulePing(){
        pingScheduler.shutdownNow();
    }
    public void stopScheduleConnect(){
        connectScheduler.shutdownNow();
    }

    private void doReconnect() {
        if (!isLogin) {
            this.reconnect();
        }
    }

    private void doPingStr() {
        if (isLogin) {
            this.send(pingString);
        }
    }
}

