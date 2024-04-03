package com.pim.server.client.observer;

import com.alibaba.fastjson.JSONObject;
import com.pim.server.beans.MessageBody;
import com.pim.server.client.socket.PriWebSocketClient;
import com.pim.server.utils.EncryptionDecryptionUtils;


import javax.net.ssl.*;
import java.net.Socket;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class PriManager {

    private LinkedList<String> linkedList = new LinkedList<>();
//    private static PriManager priManager;


    public String imIPAndPort = "";
    public String fromUid = "";
    public String token = "";
    public String deviceId = "";


    public PriWebSocketClient priWebSocketClient;
    public PriManagerSubject priManagerSubject;

    ScheduledExecutorService resendScheduler;


    public PriManager() {
        priManagerSubject = new PriManagerSubject();
    }

//    public static PriManager instance() {
//        if (null == priManager) {
//            priManager = new PriManager();
//        }
//        return priManager;
//    }

    public void setSSL() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{MyTrustManager.trustManager}, null);
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();
            priWebSocketClient.setSocketFactory(socketFactory);
        } catch (Exception e) {

        }
    }

    /**
     * Initialize and start
     */
    public void startSocket() {

        try {
            URI serverUri = URI.create(imIPAndPort);
            priWebSocketClient = new PriWebSocketClient(serverUri);
            if (imIPAndPort.indexOf("wss://") >= 0) {
                setSSL();
            }
            priWebSocketClient.priManagerSubject = priManagerSubject;
            priWebSocketClient.fromUid = fromUid;
            priWebSocketClient.token = token;
            priWebSocketClient.deviceId = deviceId;
            priWebSocketClient.start();
            scheduleResend();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * stop
     */
    public void stopSocket() {
        try {
            priWebSocketClient.stopSchedulePing();
            priWebSocketClient.stopScheduleConnect();
            priWebSocketClient.close();
        }catch (Exception exception){
            exception.printStackTrace();
        }


    }

    /**
     * Send a message
     *
     * @param messageBody
     * @return
     */
    public boolean sendMessage(MessageBody messageBody) {
        boolean sendrs = false;
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(messageBody);
        String str = EncryptionDecryptionUtils.encrypt(EncryptionDecryptionUtils.getUidKey(fromUid), jsonObject.toJSONString());
        if (priWebSocketClient.isLogin) {
            priWebSocketClient.send(str);
            sendrs = true;
        } else {
            linkedList.addFirst(str);
        }
        return sendrs;
    }

    public boolean sendMessage(String message) {
        boolean sendrs = false;
        String str = EncryptionDecryptionUtils.encrypt(EncryptionDecryptionUtils.getUidKey(fromUid), message);
        if (priWebSocketClient.isLogin) {
            priWebSocketClient.send(str);
            sendrs = true;
        } else {
            linkedList.addFirst(str);
        }
        return sendrs;
    }

    public boolean sendMessageNoEncr(String message) {
        boolean sendrs = false;
        if (priWebSocketClient.isLogin) {
            priWebSocketClient.send(message);
            sendrs = true;
        } else {
            linkedList.addFirst(message);
        }
        return sendrs;
    }


    private void scheduleResend() {
        resendScheduler = Executors.newSingleThreadScheduledExecutor();
        long delay = 10; //Delay execution time (seconds)
        long period = 5; //Execution interval (seconds)
        resendScheduler.scheduleAtFixedRate(this::checkReSend, delay, period, TimeUnit.SECONDS);
    }

    private void checkReSend() {
        if (priWebSocketClient.isLogin) {
            while (!linkedList.isEmpty()) {
                priWebSocketClient.send(linkedList.removeLast());
            }
        }
    }


}