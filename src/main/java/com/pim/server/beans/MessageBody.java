package com.pim.server.beans;


import lombok.Data;

@Data
public class MessageBody {

    String eventId = "";//事件ID，参考事件ID文件

    String fromUid = "";//发送者ID
    String deviceId = "";//唯一设备id，目前用AFID作为标识，登录时带入
    String token = "";//发送者token

    String toUid = "";//接收者ID，多个以逗号隔开  重点：对于客户端发送过来的消息，不能和groupId并存，两者只能同时出现一个

    String mType = "";//消息类型
    String cTimest = "";//客户端发送时间搓
    String sTimest = "";//服务端接收时间搓
    String dataBody = "";//消息体，可以自由定义，以字符串格式传入{}

    String isGroup = "0";//是否群组 1-群组，0-个人
    String groupId = "";//群组ID ，对于客户端发送过来的消息，不能和toUid并存，两者只能同时出现一个
    String groupName = "";//群组名称

    String isAck = "0";//客户端接收到服务端发送的消息后，返回的状态= 1；dataBody结构 sTimest,sTimest,sTimest,sTimest......

    String isCache = "0";//是否需要存离线 1-需要，0-不需要

    String channelId = "";//用户的channel


}
