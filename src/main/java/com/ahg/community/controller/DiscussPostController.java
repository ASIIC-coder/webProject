package com.ahg.community.controller;


import com.ahg.community.entity.Comment;
import com.ahg.community.entity.DiscussPost;
import com.ahg.community.entity.Page;
import com.ahg.community.entity.User;
import com.ahg.community.service.CommentService;
import com.ahg.community.service.DiscussPostService;
import com.ahg.community.service.LikeService;
import com.ahg.community.service.UserService;
import com.ahg.community.util.CommunityConstant;
import com.ahg.community.util.CommunityUtil;
import com.ahg.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;


    //访问路径
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content){
        User user = hostHolder.getUser();
        if(user == null){
            return CommunityUtil.getJSONString(403,"您还没有登录!");
        }
        //浏览器提交的帖子的内容
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        //调用service方法存储
        discussPostService.addDiscussPost(post);
        return CommunityUtil.getJSONString(0,"发布成功");

    }

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        //帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",post);
        //帖子作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user",user);
        //帖子的点赞
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,discussPostId);
        model.addAttribute("likeCount", likeCount);

        //点赞状态,（当前用户）
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);//返回给页面

        //评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());// 从帖子中获取评论的数量

        //评论：给帖子的评论
        //回复：给评论的评论
        //评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());

        //评论Vo列表（显示对象）
        List<Map<String, Object>> commentVoList = new ArrayList<>();//实例化用来封装评论的Arraylist集合
        if(commentVoList != null){
            for(Comment comment : commentList){//每次遍历commentList的集合得到一个comment
                Map<String, Object> commentVo = new HashMap<>(); //实例化一个map集合，用来封装呈现给页面的数据--一个评论的Vo
                //向Vo添加评论
                commentVo.put("comment", comment);
                //向Vo添加作者
                commentVo.put("user", userService.findUserById(comment.getUserId()));

                //点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeCount", likeCount);
                //点赞状态
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);

                //查询回复---list
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);//从Id为0开始查询所有

                //回复的Vo列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if(replyList != null){
                    for(Comment reply : replyList){
                        Map<String, Object> replyVo = new HashMap<>();
                        //回复
                        replyVo.put("reply",reply);
                        //作者
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        //回复的对象
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target",target);
                        //点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);

                        replyVoList.add(replyVo);//将回复装载在集合中
                    }
                }
                commentVo.put("replies", replyVoList);//返回所有回复的列表信息

                //回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);//添加所有的commentVo到Vo中
            }
        }
        model.addAttribute("comments",commentVoList);

        return "/site/discuss-detail";
    }
}
