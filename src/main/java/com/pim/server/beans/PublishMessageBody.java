package com.pim.server.beans;

import lombok.Data;

import java.io.Serializable;


@Data
public class PublishMessageBody implements Serializable {

    private static final long serialVersionUID = 100001L;

    String toUid = "";
    String message = "";

}
