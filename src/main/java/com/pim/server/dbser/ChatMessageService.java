package com.pim.server.dbser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pim.server.beans.Author;
import com.pim.server.beans.DataBody;
import com.pim.server.beans.MessageBody;
import com.pim.server.constants.CommParameters;
import com.pim.server.events.CommEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChatMessageService {


    public static void save(MessageBody messageBody){


        String databody = getFormatJsonString(messageBody.getDataBody());

        JSONObject jsonObject = JSONObject.parseObject(databody);
        if(!jsonObject.containsKey("author")){
            jsonObject = null;
            return;
        }
        jsonObject = null;

        // Belongs to forwarding
        if(messageBody.getToUid().length() > 0 && messageBody.getGroupId().length() > 0){
            return;
        }


        String fromUid = messageBody.getFromUid();
        String toUid = messageBody.getToUid();
        String groupId = messageBody.getGroupId();

        DataBody dataBody = JSON.parseObject(messageBody.getDataBody(), DataBody.class);

        String sql_message_insert = "insert into chat_messages(from_uid,to_uid,group_id,m_id,m_type,m_text,file_name,file_size,img_height,img_width,file_uri,created_time) values(?,?,?,?,?,?,?,?,?,?,?,?)";

        Connection conn = null;
        PreparedStatement ptmt = null;
        ResultSet rs = null;

        try {

            conn = CommParameters.instance().getLiveDataSource().getConnection();

            ptmt = conn.prepareStatement(sql_message_insert); //预编译SQL，减少sql执行

            ptmt.setString(1, fromUid);
            ptmt.setString(2, toUid);
            ptmt.setString(3, groupId);

            ptmt.setString(4, dataBody.getId());
            ptmt.setString(5, dataBody.getType());

            ptmt.setString(6, dataBody.getText());

            ptmt.setString(7, dataBody.getName());
            ptmt.setDouble(8, dataBody.getSize());
            ptmt.setDouble(9, dataBody.getHeight());
            ptmt.setDouble(10, dataBody.getWidth());
            ptmt.setString(11, dataBody.getUri());
            ptmt.setString(12, dataBody.getCreatedAt().toString());

            ptmt.executeUpdate();

        }catch (Exception exception){
            exception.printStackTrace();
        }finally {
            CommEvent.closeDbConn(conn, ptmt, rs);
        }

    }

    public static String getFormatJsonString(String json) {
        json=json.replaceAll("\"\\{", "\\{");
        json=json.replaceAll("\\}\"", "\\}");
        return json;
    }



    public static String getChatList(String fromUid,String toUid,int startPage) {

        int start = startPage * 8;

        String sql = "SELECT " +
                " a.ids, " +
                " a.m_id, " +
                " a.from_uid, " +
                " a.to_uid, " +
                " a.group_id, " +
                " a.m_type, " +
                " a.m_text, " +
                " a.mime_type, " +
                " a.file_name, " +
                " a.file_size, " +
                " a.img_height, " +
                " a.img_width, " +
                " a.file_uri, " +
                " a.created_time, " +
                " b.author_id, " +
                " b.first_name, " +
                " b.last_name, " +
                " b.head_img  " +
                "FROM " +
                " chat_messages a " +
                " LEFT JOIN chat_author b ON a.from_uid = b.author_id  " +
                " WHERE (a.from_uid = ? AND a.to_uid = ?) " +
                " OR (a.from_uid = ? AND a.to_uid = ?) " +
                "ORDER BY " +
                " a.ids DESC LIMIT "+start+",8";


        System.out.println(sql);

        Connection conn = null;
        PreparedStatement ptmt = null;
        ResultSet rs = null;

        JSONObject jsonObject = new JSONObject();


        try {

            conn = CommParameters.instance().getLiveDataSource().getConnection();
            ptmt = conn.prepareStatement(sql); //预编译SQL，减少sql执行
            ptmt.setString(1,fromUid);
            ptmt.setString(2,toUid);
            ptmt.setString(3,toUid);
            ptmt.setString(4,fromUid);
            rs = ptmt.executeQuery();

            JSONArray jsonArray = new JSONArray();
            int index = 0;
            while (rs.next()) {


                Author author = new Author();

                author.setId(rs.getString("author_id"));
                author.setFirstName(rs.getString("first_name"));
                author.setLastName(rs.getString("last_name"));
                author.setImageUrl(rs.getString("head_img"));

                DataBody dataBody = new DataBody();
                dataBody.setAuthor(author);
                dataBody.setId(rs.getString("m_id"));
                dataBody.setType(rs.getString("m_type"));
                dataBody.setText(rs.getString("m_text"));
                dataBody.setMimeType(rs.getString("mime_type"));
                dataBody.setName(rs.getString("file_name"));
                dataBody.setSize(rs.getDouble("file_size"));
                dataBody.setHeight(rs.getDouble("img_height"));
                dataBody.setWidth(rs.getDouble("img_width"));
                dataBody.setUri(rs.getString("file_uri"));
                dataBody.setCreatedAt(rs.getTimestamp("created_time"));

                JSONObject dataBodyJson = (JSONObject) JSON.toJSON(dataBody);
                jsonArray.add(index,dataBodyJson);
                index++;

            }

            jsonObject.put("data",jsonArray);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            CommEvent.closeDbConn(conn, ptmt, rs);
        }
        return jsonObject.toJSONString();
    }

}
