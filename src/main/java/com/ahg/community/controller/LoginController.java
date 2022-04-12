package com.ahg.community.controller;

import com.ahg.community.entity.User;
import com.ahg.community.service.UserService;
import com.ahg.community.util.CommunityConstant;
import com.ahg.community.util.CommunityUtil;
import com.ahg.community.util.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userservice;

    @Autowired
    private Producer kaptchaProducer;//注入配置类的bean用于生成验证码

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage(){//获取注册页面

        return "/site/register";
    }


    //返回页面
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage(){//获取注册页面

        return "/site/login";
    }



    /**
     * 注册邮箱的逻辑方法
     * @param model
     * @return
     */
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String, Object> map = userservice.register(user);
        if(map == null || map.isEmpty()){
            model.addAttribute("msg", "注册成功,激活邮件已发送到您邮箱！");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else{
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    //http://localhost:8582/community/activation/101/code
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
                                            //从路径中取值 userId、code
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code){
        int result = userservice.activation(userId, code);
        if(result == ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功，可以正常使用您的账号");
            model.addAttribute("target","/login");
        }else if(result == ACTIVATION_REPEAT){
            model.addAttribute("msg","激活码无效，此账号已激活");
            model.addAttribute("target","/index");
        } else{
            model.addAttribute("msg","激活失败，此激活码不匹配");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/){
        //生成验证码需要配置类的Bean
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);
        // 将验证码存入session
//        session.setAttribute("kaptcha", text);
        //验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        //将验证码存入Redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);


        //将图片输出给浏览器
        response.setContentType("image/png"); //声明传给浏览器的参数类型
        try {
            //获取输出流
            ServletOutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败:" + e.getMessage());
        }

    }

    @RequestMapping(path = "/login",method = RequestMethod.POST)//此处的login的访问是post，登录首页是get页面，此方法是login完成登录请求
    public String login(String username, String password, String code, boolean rememberMe,
                        Model model, HttpServletResponse response,@CookieValue("kaptchaOwner") String kaptchaOwner){

        String kaptcha = null;
        if(StringUtils.isNotBlank(kaptchaOwner)){//判断key是否为空---数据是否失效
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);//得到key
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);//得到value---验证码
        }
        //检查验证码
//        String kaptcha = (String) session.getAttribute("kaptcha");
        if(StringUtils.isBlank(kaptcha) ||  StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","验证码不正确");
            return "/site/login";
        }

        //检查账号、密码
        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> loginMap = userservice.login(username, password, expiredSeconds);
        if(loginMap.containsKey("ticket")){
            //把ticket取出来让客户端存储--cookie存储
            Cookie cookie = new Cookie("ticket", loginMap.get("ticket").toString());
            cookie.setPath("contextPath"); //存储的路径
            cookie.setMaxAge(expiredSeconds); //存储时限
            response.addCookie(cookie); //cookie返回到请求的客户端
            return "redirect:/index";
        }else {
            model.addAttribute("usernameMsg", loginMap.get("usernameMsg"));
            model.addAttribute("passwordMsg", loginMap.get("passwordMsg"));
            return "/site/login";
        }
    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userservice.logout(ticket);
        return "redirect:/login";//重定向到Login页面
    }
}
