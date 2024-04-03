package com.pim.server.client;


import com.alibaba.fastjson.JSONObject;
import com.pim.server.beans.MessageBody;
import com.pim.server.client.observer.PriManager;
import com.pim.server.client.observer.PriObserver;
import com.pim.server.constants.CommParameters;
import com.pim.server.utils.TimeUtils;


public class PriImClient implements PriObserver {



    public String fromUid = "1001_30320";
    public String token = "0000";
    public String deviceId = TimeUtils.getNanoTime() + "";
    public String serverIp = "wss://im.polarmeta.cc"; //预发布IM

    public PriManager priManager;



    public static void main(String[] args) {
        PriImClient priImClient = new PriImClient();
        priImClient.init();
    }


    public void init(){
        priManager = new PriManager();
        priManager.imIPAndPort = serverIp;
        priManager.fromUid = fromUid;
        priManager.token = token;
        priManager.deviceId = deviceId;
        priManager.priManagerSubject.addObserver(this);
        priManager.startSocket();
    }



    @Override
    public void onIMMessage(String message) {

        JSONObject jsonObject = JSONObject.parseObject(message);
        if(jsonObject.containsKey("resDesc")){
            if(jsonObject.getString("resDesc").indexOf("Login successful") >= 0){
                CommParameters.instance().getOnlineServer().put(serverIp,this);
            }
        }
        System.out.println("im message received:" + message);

    }

    @Override
    public void onIMError(String message) {

        JSONObject jsonObject = JSONObject.parseObject(message);
        if(jsonObject.containsKey("imcode")){
            int imcode = Integer.parseInt(jsonObject.getString("imcode"));
            System.out.println("imcode="+imcode);
            if(imcode == 501 || imcode == 502){
                priManager.stopSocket();
                CommParameters.instance().getOnlineServer().remove(serverIp);
                System.out.println("stop socket " + serverIp);
            }
        }

        System.out.println("im error message received:" + message);
    }

    public void sendMessage(String message){
        priManager.sendMessage(message);
    }

    public void sendMessage(MessageBody messageBody){
        priManager.sendMessage(messageBody);
    }

    public void sendMessageNoEncr(String message){
        priManager.sendMessageNoEncr(message);
    }






}
