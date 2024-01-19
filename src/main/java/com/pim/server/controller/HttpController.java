package com.pim.server.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.pim.server.beans.HttpResBody;
import com.pim.server.dbser.ChatMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Component
@Slf4j
@RestController
@RequestMapping("/")
public class HttpController {



    @PostMapping("/savemessage")
    public String saveMessage(@RequestParam("data") String data) {
        String rs = makeResString("savemessage",1,"ok");
        return rs;
    }


    @PostMapping("/getchatlist")
    @CrossOrigin(origins = "*")
    public String getChatList(@RequestParam("data") String data) {
        //System.out.println(data);
        JSONObject jsonObject = JSONObject.parseObject(data);
        String rs = ChatMessageService.getChatList(jsonObject.getString("from_uid"),jsonObject.getString("to_uid"),jsonObject.getInteger("start_page"));
        //System.out.println(rs);
        return rs;
    }

    @PostMapping("/fileupload")
    public String fileupload(@RequestParam("imgFile") MultipartFile file, @RequestParam("imgName") String name) throws Exception {
        // 设置上传至项目文件夹下的uploadFile文件夹中，没有文件夹则创建
        File dir = new File("uploadfile");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String filepath = dir.getAbsolutePath() + File.separator + name;
        file.transferTo(new File(filepath));
        String httrs = uploadFile(filepath,name);
        return httrs;
    }



    private String makeResString(String eventId,int status,String desc ){
        HttpResBody httpResBody = new HttpResBody();
        httpResBody.setEventId(eventId);
        httpResBody.setStatus(status);
        httpResBody.setDesc(desc);
        JSONObject jsonObject = (JSONObject) JSON.toJSON(httpResBody);
        return jsonObject.toJSONString();
    }


    private final static String MY_ACCESS_KEY_ID = "xxxxxxxx";
    private final static String MY_PICTURE_BUCKET = "xxxx";
    private final static String MY_SECRET_KEY = "xxx+xxxxxx/xxxxxxxxxxxx";

    private String uploadFile(String filepath,String fileName){
        int code = -1;
        int loopNum = 0;

        File file = new File(filepath);
        while(code != 0 && loopNum < 5) {
            try {
                System.out.println(Thread.currentThread().getName() + "执行上传:" + filepath);

                String fileNameToUpload = "xxx/"+fileName;
                AmazonS3Client s3Client = new AmazonS3Client(new BasicAWSCredentials(MY_ACCESS_KEY_ID, MY_SECRET_KEY));
                PutObjectRequest request = new PutObjectRequest(MY_PICTURE_BUCKET, fileNameToUpload, file);
                request.setCannedAcl(CannedAccessControlList.PublicRead);
                PutObjectResult result = s3Client.putObject(request);
                code = 0;
                System.out.println(filepath + "上传完成：OK" +result.getContentMd5());

            } catch (Exception e) {
                e.printStackTrace();
                code = -1;
                System.out.println(Thread.currentThread().getName() + "上传失败：ERROR");
            }
            loopNum = loopNum + 1 ;
        }
        file.delete();

        String rs = "https://xxxx.xxxx.com/imchatimg/"+fileName;
        return rs;
    }
}
