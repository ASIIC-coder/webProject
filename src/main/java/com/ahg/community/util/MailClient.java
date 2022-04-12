package com.ahg.community.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class MailClient { //MailClient类为一个客户端
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);


    /**
     * mailSender组件自动注入
     */
    @Autowired
    private JavaMailSender mailSender;  //

    @Value("${spring.mail.username}") //通过key将username注入到bean中.
    private String from;        //发件人

    /**
     * 定义发送邮件方法-->格式、内容、主题的定义
     * @param to
     * @param subject
     * @param content
     */
    public void sendMail(String to, String subject, String content){

        try {
            //构建邮件的主体模板
            MimeMessage message = mailSender.createMimeMessage();
            //构建邮件的详细内容
            MimeMessageHelper helper = new MimeMessageHelper(message);
            //set发件人
            helper.setFrom(from);
            //set收件人
            helper.setTo(to);
            //set邮件的主题
            helper.setSubject(subject);
            //set邮件的正文内容
            helper.setText(content, true); //content文本为true允许支持html文件
            //set发送邮件
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


}
