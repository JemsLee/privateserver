package com.pim.server.netty;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class PrivateChannelSupervise {

    private static ChannelGroup GlobalGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    //组Map
    private static ConcurrentMap<String, ChannelGroup> GroupMap = new ConcurrentHashMap();

    public static void addChannel(Channel channel) {
        GlobalGroup.add(channel);
    }

    public static void removeChannel(Channel channel) {
        GlobalGroup.remove(channel);
    }

    public static Channel findChannel(String id) {
        Optional<Channel> channelOptional = GlobalGroup.stream().filter(m -> m.id().asLongText().equals(id)).findFirst();
        return channelOptional.orElse(null);
    }

    public static void send2All(TextWebSocketFrame tws) {
        GlobalGroup.writeAndFlush(tws);
    }


    //用户id=>channel示例
    private static ConcurrentHashMap<String, Channel> channelMap = new ConcurrentHashMap<>();

    /**
     * 判断一个通道是否有用户在使用
     * 可做信息转发时判断该通道是否合法
     * @param channel
     * @return
     */
    public static boolean hasUser(Channel channel) {
        AttributeKey<String> key = AttributeKey.valueOf("user");
        return (channel.hasAttr(key) || channel.attr(key).get() != null);//netty移除了这个map的remove方法,这里的判断谨慎一点
    }



    /**
     * 上线一个用户
     *
     * @param channel
     * @param userId
     */
    public static void online(Channel channel, String userId) {
        //先判断用户是否在web系统中登录?
        //这部分代码个人实现,参考上面redis中的验证

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

    /**
     * 根据用户id获取该用户的通道
     *
     * @param userId
     * @return
     */
    public static Channel getChannelByUserId(String userId) {
        return channelMap.get(userId);
    }

    /**
     * 判断一个用户是否在线
     *
     * @param userId
     * @return
     */
    public static Boolean isOnline(String userId) {
        return channelMap.containsKey(userId) && channelMap.get(userId) != null;
    }


}
