package com.example.livestream.Service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ChannelService {

    @Autowired
    private StringRedisTemplate redisTemplate;


    private static final String CHANNEL_PREFIX="Channel:";

    public boolean startStream(int channelid,String streamerid){

        String key=CHANNEL_PREFIX+channelid;

        String live=(String)redisTemplate.opsForHash().get(key,"live"); // this return type is object so cast with string

        if("true".equals(live)){
            System.out.println("Already Streaming,Only One Streaming Session Allowed");
            return false;
        }
        redisTemplate.opsForHash().put(key,"live","true");
        redisTemplate.opsForHash().put(key,"streamerId",streamerid);

        redisTemplate.opsForHash().putIfAbsent(key,"viewers","0");

        return true;
    }

    public void StopStream(int channelId){
        String key=CHANNEL_PREFIX+channelId;

        redisTemplate.opsForHash().put(key, "live", "false");
    }

    public boolean addViewer(int channelId){
        String key=CHANNEL_PREFIX+channelId;


       String viewers=(String)redisTemplate.opsForHash().get(key,"viewers");
       int numberofviewers=viewers!=null ?Integer.parseInt(viewers):0;
       if(numberofviewers<10){
           redisTemplate.opsForHash().increment(key,"viewers",1);
           //when using increment it converts string to integer increments it and convverts it back to string
           //all values stored internally as strings\
           return true;
       }
       else{
           return false;
       }

    }
    public boolean  deleteViewer(int channelId){
        String key=CHANNEL_PREFIX+channelId;
        String viewers=(String)redisTemplate.opsForHash().get(key,"viewers");
        int numberofviewers=viewers!=null ?Integer.parseInt(viewers):0;
        if(numberofviewers<=0){
            return false;
        }
        else{
            redisTemplate.opsForHash().increment(key,"viewers",-1);
            return true;
        }
    }

    public Map<Object,Object> getstate(int channelId){
        String key=CHANNEL_PREFIX+channelId;
        return redisTemplate.opsForHash().entries(key);
    }








}
