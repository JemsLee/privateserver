package com.pim.server.beans;

import lombok.Data;

import java.io.Serializable;

///用户上下线
@Data
public class PublishOffLineBody implements Serializable {

    private static final long serialVersionUID = 100000L;

    String fromUid = "";

}
