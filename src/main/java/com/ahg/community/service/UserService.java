package com.ahg.community.service;

import com.ahg.community.dao.LoginTicketMapper;
import com.ahg.community.dao.UserMapper;
import com.ahg.community.entity.LoginTicket;
import com.ahg.community.entity.User;
import com.ahg.community.util.CommunityConstant;
import com.ahg.community.util.CommunityUtil;
import com.ahg.community.util.MailClient;
import com.ahg.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;


@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")       //用表达式的方式取key的值
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id){
//                return userMapper.selectById(id);
        User user = getCache(id);
        if(user == null){
            user = initCache(id);
        }
        return user;
    }


//    @Autowired
//    private LoginTicketMapper loginTicketMapper;



    /**
     * 邮箱注册的业务功能的实现
     * @param user
     * @return
     */
    public Map<String,Object> register(User user){
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if(user == null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }

        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if(u != null){
            map.put("usernameMsg", "该账号已存在");
            return map;
        }

        //验证邮箱
        User e = userMapper.selectByName(user.getEmail());
        if(e != null){
            map.put("emailMsg", "该邮箱已被注册");
            return map;
        }

        //注册用户

        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));//对用户密码加密并覆盖未加密的用户密码
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());//激活码
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000))); //用户注册后的随机头像

        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        //http://localhost:8582/community/activation/101/code  //点击激活链接会传递userId和code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode(); //userId为自动赋值
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(),"激活账号", content);

        return map;
    }

    public int activation(int userId, String code){
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId, 1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAILURE;
        }
    }


    /**
     * 实现登录功能返回一个Map集合
     * @param username
     * @param password
     * @param expiredSeconds
     * @return
     */


    public Map<String, Object> login(String username, String password, int expiredSeconds){
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg", "该账号不存在");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg", "密码不能为空");
            return map;
        }

        //验证账号是否存在，状态是否为激活的
        User user = userMapper.selectByName(username);
        if(user == null){
            map.put("usernameMsg","该账号不存在");
            return map;
        }

        //验证状态
        if(user.getStatus() == 0){
            map.put("usernameMsg","该账号未激活");
            return map;
        }

        //验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if(!user.getPassword().equals(password)){
            map.put("passwordMsg","密码不正确");
            return map;
        }

        //生成登陆凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);

        //获取ticket key，用来存储到redis中
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);//在redis中

        map.put("ticket", loginTicket.getTicket());
        return map;//返回登录凭证 ticket 字符串
    }

    public void logout(String ticket){
//        loginTicketMapper.updateStatus(ticket, 1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);

    }

    public LoginTicket findLoginTicket(String ticket){
//        return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);

    }

    //param: 更新用户的id, 更新头像的位置
    public int updateHeader(int userId, String headerUrl){
//        return userMapper.updateHeader(userId, headerUrl);
        int rows = userMapper.updateHeader(userId, headerUrl);//用户状态发生了改变
        clearCache(userId);//清理旧的缓存信息
        return rows;
    }


    //param: 更改用户密码
    public int updatePassword(int userId, String password){
        return userMapper.updatePassword(userId, password);
    }

    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }

    //1.优先从缓存中取值
    private User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);

    }

    //2.取不到初始化缓存数据
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;

    }

    //3.数据变更时清除缓存
    private void clearCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

}
