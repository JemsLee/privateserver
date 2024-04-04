package com.pim.server.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pim.server.beans.HttpResBody;
import com.pim.server.events.CommEvent;
import com.pim.server.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Component
@Slf4j
@RestController
@RequestMapping("/")
public class HttpController {

    @PostMapping("/generatetoken")
    @CrossOrigin(origins = "*")
    public String generateToken(@RequestParam("platformId") String platformId, @RequestParam("userId") String userId) {
        JSONObject jsonObject = new JSONObject();
        if(RedisUtils.instance().redisson.getMap("imPlatformId").containsKey(platformId)){
            String new_user_id = platformId+"_" + userId;
            String token = CommEvent.getRandomString(32);
            jsonObject.put("status",1);
            jsonObject.put("message","OK");
            jsonObject.put("loginId",new_user_id);
            jsonObject.put("loginToken",token);

            RedisUtils.instance().redisson.getBucket("token_list:"+new_user_id,new StringCodec()).set(token);

        }else {
            jsonObject.put("status",0);
            jsonObject.put("message","Platform ID does not exist.");
        }
        return jsonObject.toJSONString();
    }


}
