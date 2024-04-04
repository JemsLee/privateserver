package com.pim.server;

import com.pim.server.constants.CommParameters;
import com.pim.server.events.CommEvent;
import com.pim.server.netty.PrivateWebSocketServer;
import com.pim.server.property.RedisPropertyForPro;
import com.pim.server.property.RedisPropertyForTest;
import com.pim.server.utils.RedisUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableScheduling  //扫描定时器
@SpringBootApplication
public class PrivateMessageApplication implements CommandLineRunner, ApplicationListener<ContextClosedEvent> {

    @Autowired
    PrivateWebSocketServer privateWebSocketServer;


    @Autowired
    RedisPropertyForPro redisPropertyForPro;
    @Autowired
    RedisPropertyForTest redisPropertyForTest;

    public static void main(String[] args) {
        try {
            SpringApplication.run(PrivateMessageApplication.class, args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public void run(String... args)  {

        System.out.println(args[0]);
        String[] inputParas = args[0].split(",");

        if(inputParas.length == 5) {

            int run_time = Integer.parseInt(inputParas[0]);
            CommParameters.instance().setServerIp(inputParas[1]);
            CommParameters.instance().setServerPort(Integer.parseInt(inputParas[2]));
            CommParameters.instance().setImUser(inputParas[3]);
            CommParameters.instance().setTransitType(Integer.parseInt(inputParas[4]));


            //init Redis
            intiRedis(run_time);

            //init websocket server
            initSocketServer();

            //Redis publish and subscribe
            CommEvent.setPublish();

            //server up status
            CommEvent.setServerStatus("up");

            //throw socket connect to otherr imserver
            CommEvent.connectToOtherServer();

        }
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        //When the server is shut down, clean up the users linked to this server
        CommEvent.setServerStatus("down");
        CommEvent.clearAllUserInfo();
        RedisUtils.instance().stop();

    }

    private void intiRedis(int run_time){
        if (run_time == 1) {  //String ip,String user,String pwd, int port, int db, int cluser
            RedisUtils.instance().init(redisPropertyForTest.getIp(),redisPropertyForTest.getUser(),redisPropertyForTest.getPassword(),redisPropertyForTest.getPort(),
                    redisPropertyForTest.getDb(), redisPropertyForTest.getIscluster());
        }else {
            RedisUtils.instance().init(redisPropertyForPro.getIp(),redisPropertyForPro.getUser(),redisPropertyForPro.getPassword(),redisPropertyForPro.getPort(),
                    redisPropertyForPro.getDb(), redisPropertyForPro.getIscluster());
        }

    }

    private void initSocketServer()  {
        try {
            if (CommParameters.instance().getServerIp().length() > 0 && CommParameters.instance().getServerPort() != -1) {
                privateWebSocketServer.init(CommParameters.instance().getServerPort());
            }
        }catch (Exception exception){
            exception.printStackTrace();
            log.error("initSocketServer error={}",exception.getMessage());
        }

    }



}
