package com.ahg.community.controller;

import com.ahg.community.entity.DiscussPost;
import com.ahg.community.entity.Page;
import com.ahg.community.entity.User;
import com.ahg.community.service.DiscussPostService;
import com.ahg.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller//controller访问路径可以省略
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    //处理请求的方法
    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page){//在springboot中方法的参数由dispatchServlet初始化
        //SpringMVC会自动实例化Model,Page.并将Page注入Model
        //在thymeleaf中可以直接访问Page对象中的数据
        page.setRows(discussPostService.findDiscussPostRows(0));//首页，传入零。不以用户Id显示行数
        page.setPath("/index"); //显示页面的路径
        List<DiscussPost> list = discussPostService.findDiscussPosts(0,page.getOffset(),page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();//封装discussPost和对应的user对象
        if(list != null){
            for(DiscussPost post: list){
                Map<String, Object> map = new HashMap<>();
                map.put("post",post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);

        System.out.println("Test service!!!!");

        return "/index"; //返回路径
    }

}
