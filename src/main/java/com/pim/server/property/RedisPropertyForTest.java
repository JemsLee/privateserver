package com.pim.server.property;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "redis.test")
@Data
public class RedisPropertyForTest {
    String ip;
    int port;
    String user;
    String password;
    int db;
    int iscluster;
}
