package com.ahg.community.controller;

import com.ahg.community.entity.Event;
import com.ahg.community.entity.User;
import com.ahg.community.event.EventProducer;
import com.ahg.community.service.LikeService;
import com.ahg.community.util.CommunityConstant;
import com.ahg.community.util.CommunityUtil;
import com.ahg.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId){
        User user = hostHolder.getUser();//获取当前用户
        //点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        //数量显示
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        //点赞的状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        //统一封装为map集合传给页面
         Map<String, Object> map = new HashMap<>();
         map.put("likeCount", likeCount);
         map.put("likeStatus", likeStatus);

         //触发点赞事件的事件的构建
        if(likeStatus == 1){//判断是点赞还是取消点赞
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())//谁给我点赞？
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);

            eventProducer.fireEvent(event);

        }

         return CommunityUtil.getJSONString(0, null, map);

    }
}
