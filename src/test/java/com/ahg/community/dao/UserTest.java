package com.ahg.community.dao;

import com.ahg.community.entity.DiscussPost;
import com.ahg.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

@SpringBootTest
public class UserTest {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(101);
        System.out.println(user);

        User user1 = userMapper.selectByName("liubei");
        System.out.println(user1);

        User user2 = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user2);
    }


    @Test
    public void testInsertUser(){
        User user = new User();
        user.setUsername("test");
        user.setPassword("sos1123");
        user.setSalt("abc");
        user.setEmail("javaRaveTab.com");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void updateUser(){
        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);

        rows = userMapper.updateHeader(150, "http://www.nowcoder.com/102.png");

        System.out.println(rows);

        rows = userMapper.updatePassword(150,"hello");
        System.out.println(rows);


    }

    @Test
    public void testSelectPosts(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(149,0,10);
        for(DiscussPost post : list){
            System.out.println(post);
        }

        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);

    }

}
