package com.pim.server.beans;

import lombok.Data;

@Data
public class ActionReturnBody {

    String eventId = "";//Event ID, refer to the event ID file
    String inEventId = "";//Sender's event ID, can be empty
    String eStatus = "";//Status
    String cTimest = ""; //Client sends timestamp
    String sTimest = "";//The server sends the timestamp
    String resDesc = ""; //Reply message content

}
