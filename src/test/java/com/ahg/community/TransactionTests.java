package com.ahg.community;

import com.ahg.community.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TransactionTests {

    @Autowired
    private AlphaService alphaService;

    @Test
    public void test1() {
        Object o = alphaService.save1();
        System.out.println(o);
    }

    @Test
    public void test2() {
        Object o = alphaService.save2();
        System.out.println(o);
    }
}
