package com.pim.server.constants;

import com.pim.server.client.PriImClient;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Data
public class CommParameters {

    private static CommParameters commParameters;

    private CommParameters() {

    }

    public static CommParameters instance() {

        if (null == commParameters) {
            commParameters = new CommParameters();
            commParameters.initExecutor();
        }

        return commParameters;
    }

    private static final int CORE_POOL_SIZE = 200;
    private static final int MAX_POOL_SIZE = 20000;
    private static final int QUEUE_CAPACITY = 1;
    private static final Long KEEP_ALIVE_TIME = 1L;
    ThreadPoolExecutor executor;
    private void  initExecutor(){
        executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    boolean redisIsOk= false;
    boolean isConnectingOtherServer = false;

    String serverIp = "";
    int serverPort = -1;
    //消息中转机制，0=内部Socket中转， 1=通过Redis的发布和订阅
    int transitType = 0;

    ConcurrentHashMap<String, String> onlineUser = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, PriImClient> onlineServer = new ConcurrentHashMap<>();



}
