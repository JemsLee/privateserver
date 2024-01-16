package com.pim.server.beans;

import lombok.Data;

@Data
public class HttpResBody {

    String eventId = "";
    int status = 1;
    String desc = "";

}
