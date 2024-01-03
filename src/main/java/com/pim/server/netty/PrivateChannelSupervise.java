package com.pim.server.netty;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.concurrent.ConcurrentHashMap;


public class PrivateChannelSupervise {


    private static ConcurrentHashMap<String, Channel> channelMap = new ConcurrentHashMap<>();


    public static boolean hasUser(Channel channel) {
        AttributeKey<String> key = AttributeKey.valueOf("user");
        return (channel.hasAttr(key) || channel.attr(key).get() != null);//netty移除了这个map的remove方法,这里的判断谨慎一点
    }


    public static void online(Channel channel, String userId) {
        channelMap.put(userId, channel);
        AttributeKey<String> key = AttributeKey.valueOf("user");
        channel.attr(key).set(userId);
    }

    public static void offline(String userId) {
        channelMap.remove(userId);
    }

    public static String  getUserId(Channel channel) {
        AttributeKey<String> key = AttributeKey.valueOf("user");
        return channel.attr(key).get();
    }

    public static Channel getChannelByUserId(String userId) {
        return channelMap.get(userId);
    }

    public static Boolean isOnline(String userId) {
        return channelMap.containsKey(userId) && channelMap.get(userId) != null;
    }


}
