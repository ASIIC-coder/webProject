package com.ahg.community.service;

import com.ahg.community.dao.CommentMapper;
import com.ahg.community.entity.Comment;
import com.ahg.community.util.CommunityConstant;
import com.ahg.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit){
        return commentMapper.selectCommentsByEntity(entityType,entityId,offset,limit);
    }

    public int findCommentCount(int entityType, int entityId){
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    //增加业务方法，对数据库有两次DML操作---所以此方法需进行事务管理（当前整个方法在一个事务范围之内，采用注解方式隔离管理）
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int addComment(Comment comment){
        if(comment == null){
            throw new IllegalArgumentException("参数不能为空");
        }

        //对评论中内容过滤，传入的参数为过滤之前的内容
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent())); //过滤标签
        comment.setContent(sensitiveFilter.filter(comment.getContent()));//过滤敏感词

        int rows = commentMapper.insertComment(comment);//添加完成后，返回值为影响的行数

        //更新帖子的评论数量
        if(comment.getEntityType() == ENTITY_TYPE_POST){//判断参数类型是否为帖子
                                                        //第一个参数传入类型entityType， 第二个参数entityId
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
                                                //根据帖子的Id,将数量更新到最新的数量
            discussPostService.updateCommentCount(comment.getEntityId(), count);

        }
        return rows;
    }

    public Comment findCommentById(int id){
        return commentMapper.selectCommentById(id);
    }

}
