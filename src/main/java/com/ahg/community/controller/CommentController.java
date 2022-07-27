package com.ahg.community.controller;

import com.ahg.community.entity.Comment;
import com.ahg.community.entity.DiscussPost;
import com.ahg.community.entity.Event;
import com.ahg.community.event.EventProducer;
import com.ahg.community.service.CommentService;
import com.ahg.community.service.DiscussPostService;
import com.ahg.community.util.CommunityConstant;
import com.ahg.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;


    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {

        System.out.println("请求开始执行！！！" + discussPostId);
        int id = hostHolder.getUser().getId();
        System.out.println(id);
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        //触发评论事件event的构建
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);
        //entityUserId的确定需要先做查询
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());//得到评论的目标
            event.setEntityUserId(target.getUserId());//得到帖子作者id
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setUserId(target.getUserId());//得到键盘侠的id
        }
        //触发事件
        eventProducer.fireEvent(event);

        return "redirect:/discuss/detail/" + discussPostId;
    }


}
