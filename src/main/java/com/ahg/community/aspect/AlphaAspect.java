package com.ahg.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class AlphaAspect {

    //定义切点
    @Pointcut("execution(* com.ahg.community.service.*.*(..))")
    public void pointcut() {

    }

    //定义通知
    //在连接点开始之前记录日志
    @Before("pointcut()")
    public void before() {
        System.out.println("before,already log ");
    }

    @After("pointcut()")
    public void after() {
        System.out.println("after");

    }

    @AfterReturning("pointcut()")
    public void afterReturning() {
        System.out.println("afterReturning");

    }

    @AfterThrowing("pointcut()")
    public void afterThrowing() {
        System.out.println("afterThrowing");
    }

//    @Around("pointcut()")
//    public Object around(ProceedingJoinPoint joinPoint ) throws Throwable {
//        System.out.println("around before");
//        Object o = joinPoint.proceed();//调用处理目标组件的
//        System.out.println();
//        return o;
//    }

}
