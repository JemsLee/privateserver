package com.pim.server.beans;

import lombok.Data;

@Data
public class ActionReturnBody {

    String eventId = "";//事件ID，参考事件ID文件
    String inEventId = "";//发送者的事件ID，可以为空
    String eStatus = "";//状态
    String cTimest = "";//客户端发送时间戳
    String sTimest = "";//服务端发送时间戳
    String resDesc = "";//回复消息内容

}
