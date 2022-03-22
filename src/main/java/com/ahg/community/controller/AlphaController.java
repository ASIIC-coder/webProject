package com.ahg.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Controller
@RequestMapping("/alpha")
public class AlphaController {



    @RequestMapping("/hello")
    @ResponseBody
    public String SanGe(){
        return "唐门，我回来了";
    }

    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response){
        //获取请求数据
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        Enumeration<String> enumeration = request.getHeaderNames();
        while(enumeration.hasMoreElements()){
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            System.out.println(name + ": " + value);
        }
        System.out.println(request.getParameter("code "));
    }

    //从浏览器获取数据get请求方式一
    // /students?current=1&limit=20
    @RequestMapping(path = "/students", method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(
            @RequestParam(name = "current", required = false,defaultValue = "1") int current,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit){
        System.out.println(current);
        System.out.println(limit);
        return "some guys";
    }

    //从浏览器获取数据get请求方式二
    // /student/234
    @RequestMapping(path = "/student/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id){
        System.out.println(id);
        return "a student";
    }


    //向浏览器提供数据POST请求
    //POST请求处理方式
    @RequestMapping(path = "/student", method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name, int age){
        System.out.println(name);
        System.out.println(age);
        return "合格";
    }

    //响应HTML数据
    @RequestMapping(path = "/teacher", method = RequestMethod.GET)
    public ModelAndView getTeacher(){
        ModelAndView mav = new ModelAndView();
        mav.addObject("name","奥斯卡");
        mav.addObject("age",21);
        mav.setViewName("/demo/view");

        return mav;
    }


    @RequestMapping(path = "/school", method = RequestMethod.GET)
    public String getSchool(Model model){
        model.addAttribute("name","IIT");
        model.addAttribute("age",100);
        return "/demo/view";
    }

    //响应JSON数据（异步请求）
    //Java对象 -> JSON字符串 -> JS对象
    @RequestMapping(path = "/emp", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> getEmp(){
        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> emp = new HashMap<>();
        emp.put("name", "胡列娜");
        emp.put("age", 21);
        emp.put("salary", 1013);
        list.add(emp);

        emp = new HashMap<>();
        emp.put("name", "千仞雪");
        emp.put("age", 27);
        emp.put("salary", 1312);
        list.add(emp);

        emp = new HashMap<>();
        emp.put("name", "小舞");
        emp.put("age", 15);
        emp.put("salary", 99999);
        list.add(emp);


        return list;

    }




}
