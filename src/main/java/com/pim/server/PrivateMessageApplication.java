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
    public void run(String... args) throws Exception {

        String[] inputParas = args[0].split(",");

        if(inputParas.length == 3) {
            int run_time = Integer.parseInt(inputParas[0]);
            CommParameters.instance().setServerIp(inputParas[1]);
            CommParameters.instance().setServerPort(Integer.parseInt(inputParas[2]));

            //init Redis
            intiRedis(run_time);

            //init mysql
            initHikari();

            //init websocket server
            initSocketServer();

            //Redis publish and subscribe
            CommEvent.setPublish();

        }
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        //When the server is shut down, clean up the users linked to this server
        CommEvent.clearAllUserInfo();
    }

    private void intiRedis(int run_time){
        if (run_time == 1) {
            RedisUtils.instance().init(redisPropertyForTest.getIp(),redisPropertyForTest.getPort(),redisPropertyForTest.getDb(),redisPropertyForTest.getIscluster());
        }else {
            RedisUtils.instance().init(redisPropertyForPro.getIp(),redisPropertyForPro.getPort(),redisPropertyForPro.getDb(),redisPropertyForPro.getIscluster());
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


    /**
     * Database connection pool configuration
     */
    private void initHikari() {

        HikariConfig config_live = null;
        config_live = new HikariConfig("/hikari_test_chat.properties");

        //The maximum waiting time when obtaining a connection from the connection pool, in milliseconds, the default value is 30 seconds, at least 250ms
        config_live.setConnectionTimeout(60000);
        //The maximum number of connections that can be reserved in the connection pool, for example: 100, then the number of connections in the connection pool cannot exceed 100
        config_live.setMaximumPoolSize(30);
        //Minimum number of idle connections, default 10, that is to say, up to 10 idle connections can be retained in the connection pool, and more will be closed
        config_live.setIdleTimeout(3);

        CommParameters.instance().setLiveDataSource(new HikariDataSource(config_live));

    }




}
