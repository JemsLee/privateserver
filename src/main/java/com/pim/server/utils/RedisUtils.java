package com.pim.server.utils;

import com.pim.server.constants.CommParameters;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class RedisUtils {

    public RedissonClient redisson;

    public RedisUtils() {
    }

    public void init(String ip, int port, int db, int cluser) {

        Config config = new Config();
        String address = "redis://" + ip + ":" + port;
        System.out.println("Start connecting redis:" + address + "  db index:" + db);
        if (cluser == 0) {
            config.useSingleServer().
                    setDatabase(db).
                    setConnectionMinimumIdleSize(10).
                    setConnectionPoolSize(500)
                    .setAddress(address);
        } else if (cluser == 1) {
            config.useClusterServers()
                    .setMasterConnectionMinimumIdleSize(10)
                    .setMasterConnectionPoolSize(1000)
                    .setSlaveConnectionMinimumIdleSize(10)
                    .setSlaveConnectionPoolSize(1000)
                    .setSubscriptionConnectionMinimumIdleSize(1)
                    .setSubscriptionConnectionPoolSize(1000)
                    .setSubscriptionsPerConnection(5)
                    .setScanInterval(2000) // 集群状态扫描间隔时间，单位是毫秒
                    .addNodeAddress(address);
        }

        config.setCodec(new org.redisson.client.codec.StringCodec());
        redisson = Redisson.create(config);

        System.out.println("Successful connected redis:" + address + "  db index:" + db);

        CommParameters.instance().setRedisOk(true);
    }

    public RedissonClient getRedissonClient() {
        return redisson;
    }

    private static RedisUtils redisUtils;

    public static RedisUtils instance() {

        if (null == redisUtils) {
            redisUtils = new RedisUtils();
        }
        return redisUtils;
    }
}
