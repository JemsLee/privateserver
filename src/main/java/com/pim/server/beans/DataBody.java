package com.pim.server.beans;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class DataBody {

    Author author;
    Timestamp createdAt;
    String id = "";
    String status = "seen";
    String text = "";
    String type = "";//[text]  or [file]  or image

    //file
    String mimeType = ""; //application/pdf
    String name = "";
    double size = 0;

    //images,,,,[name] [size] like file
    double height = 0;
    double width = 0;
    String uri = "";



}
