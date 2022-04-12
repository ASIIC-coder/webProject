package com.ahg.community.service;

import com.ahg.community.dao.DiscussPostMapper;
import com.ahg.community.dao.UserMapper;
import com.ahg.community.entity.DiscussPost;
import com.ahg.community.entity.User;
import com.ahg.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;

@Service
public class AlphaService {


    //假定一个业务,在Service访问User和帖子
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    //声明式业务的管理方式demo方法
    //
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)//注解方式进行事务管理，隔离级别为二级
                                                        //配置事务时，还需要配置传播机制 required、required_new、nested三种常用常量
    public Object save1(){
        //新增用户
        User user = new User();
        user.setSalt("alpha");
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
        user.setEmail("adfas@183.com");
        user.setHeaderUrl("http://inagas.anq.com/head/99t.png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);//将用户插入到数据库中 在userMapper中 插入完成后会生成Id赋给-->keyProperty = "id"
                                    //然后mybatis向数据库请求此用户Id

        //新增帖子
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId()); //向数据库请求此Id
        post.setTitle("hola");
        post.setContent("新人报道");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);

        Integer.valueOf("avd");//制造错误

        return "ok";
    }

    public Object save2(){
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        
        //传入回调接口
        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                //新增用户
                User user = new User();
                user.setSalt("Beta");
                user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
                user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
                user.setEmail("adfas@183.com");
                user.setHeaderUrl("http://inagas.anq.com/head/99t.png");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);//将用户插入到数据库中 在userMapper中 插入完成后会生成Id赋给-->keyProperty = "id"
                //然后mybatis向数据库请求此用户Id

                //新增帖子
                DiscussPost post = new DiscussPost();
                post.setUserId(user.getId()); //向数据库请求此Id
                post.setTitle("垃圾");
                post.setContent("垃圾项目");
                post.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(post);

                Integer.valueOf("avd");//制造错误

                return "ok";
            }
        });
    }

}
