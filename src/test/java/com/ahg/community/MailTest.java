package com.ahg.community;


import com.ahg.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTest {
    @Autowired
    private MailClient mailClient;

    //主动调用thymeleaf模板
    @Autowired
    private TemplateEngine templateEngine;


    @Test
    public void testTextMail() {

        mailClient.sendMail("2272590304@qq.com", "Net Test", "Hola mejo");
    }

    @Test
    public void testHtmlMail() {

        Context context = new Context(); //实例化thymeleaf模板引擎对象
        context.setVariable("username", "sunday"); //构建对象的形参结构

        //调用模板引擎生成动态网页
        String content = templateEngine.process("/mail/demo", context);//将存在的模板引擎的路径、和模板引擎对象作为参数传入方法
        System.out.println(content);

        mailClient.sendMail("2272590304@qq.com", "HTML", content);
    }
}


