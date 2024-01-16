package com.pim.server.beans;


import lombok.Data;

@Data
public class MessageBody {

    String eventId = "";//Event ID, refer to the event ID file

    String fromUid = "";//Sender ID
    String deviceId = "";//The unique device id,
    String token = "";//sender token

    String toUid = "";//Receiver ID, multiple separated by commas. Important: For messages sent by the client, they cannot coexist with groupId. Only one of them can appear at the same time.

    String mType = "";//Message type
    String cTimest = "";//The client sends the time
    String sTimest = "";//The server receives the time
    String dataBody = "";//The message body can be freely defined and passed in {} in string format

    String isGroup = "0";//Whether the group is 1-group, 0-individual
    String groupId = "";//Group ID, for messages sent by the client, cannot coexist with toUid, and only one of them can appear at the same time.
    String groupName = "";//Group name

    String isAck = "0"; //After the client receives the message sent by the server, the returned status = 1; dataBody structure sTimest, sTimest, sTimest, sTimest...

    String isCache = "0";//Whether it needs to be stored offline 1-required, 0-not required

    String channelId = "";//User's channel


}
