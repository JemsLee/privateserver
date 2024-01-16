package com.pim.server.schedul;

import com.pim.server.utils.RedisUtils;
import com.pim.server.utils.TimeUtils;

import java.util.LinkedList;

public class CacheMessageHelper extends Thread {

    String userId;
    LinkedList<String> linkedList;
    @Override
    public void run() {
        super.run();

        String offlineKey = userId + "_offline";
        while (!linkedList.isEmpty()) {
            try {
                RedisUtils.instance().getRedissonClient().getMap(offlineKey).put(TimeUtils.getNanoTime()+"",linkedList.removeLast());
                Thread.sleep(200);
            }catch (Exception exception){
                exception.printStackTrace();
            }
        }
    }

}
