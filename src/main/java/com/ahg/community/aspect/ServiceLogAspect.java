package com.ahg.community.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Aspect
public class ServiceLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    //定义切点
    @Pointcut("execution(* com.ahg.community.service.*.*(..))")
    public void pointcut() {
    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {//JoinPoint连接点指代程序织入的目标，
        //用户[101.123.21.1],在[],访问了[com.ahg.community.service.xxx]
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {//消费者事件业务层是EventConsumer直接调用,所以没有调用controller中的方法，没有记录日志。
            return;
        }
        HttpServletRequest request = attributes.getRequest();//得到Request当前Aop拦截的是service---切点为service*,因为所有访问controller层调用service的业务逻辑

        String ip = request.getRemoteHost();//得到ip
        String s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        //得到代理目标的类型 + 方法名
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        logger.info(String.format("用户[%s],访问了[%s]", ip, s, target));
    }
}
