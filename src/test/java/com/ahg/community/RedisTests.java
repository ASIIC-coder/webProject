package com.ahg.community;


import org.aspectj.lang.annotation.Aspect;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)

public class RedisTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void test1() {
        String redisKey = "test:count";
        //存储
        redisTemplate.opsForValue().set(redisKey, 1);
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        //实现key对应的值加一的操作
        System.out.println(redisTemplate.opsForValue().increment(redisKey));

    }

    @Test
    public void test2() {//访问hash
        //声明一个key
        String redisKey = "test:user";
        //存储一个hash                  key          值的key--id 值的value 1
        redisTemplate.opsForHash().put(redisKey, "id", 1);
        redisTemplate.opsForHash().put(redisKey, "username", "陈平安");

        System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey, "username"));
    }


    @Test
    public void testList3() {
        String redisKey = "test:ids";

        redisTemplate.opsForList().leftPush(redisKey, 101);
        redisTemplate.opsForList().leftPush(redisKey, 102);
        redisTemplate.opsForList().leftPush(redisKey, 103);

        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey, 0));
        System.out.println(redisTemplate.opsForList().range(redisKey, 0, 2));

        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
    }


    @Test
    public void testSets() {
        String redisKey = "test:teachers";

        redisTemplate.opsForSet().add(redisKey, "刘咸阳", "松鹤", "西凤", "阿凡达", "华清");

        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));

    }


    @Test
    public void testSortedSets() {
        String redisKey = "test:students";

        redisTemplate.opsForZSet().add(redisKey, "阿瑙", 90);
        redisTemplate.opsForZSet().add(redisKey, "武器", 12);
        redisTemplate.opsForZSet().add(redisKey, "全文", 79);
        redisTemplate.opsForZSet().add(redisKey, "暗黑", 31);
        redisTemplate.opsForZSet().add(redisKey, "芬达", 12);


        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        System.out.println(redisTemplate.opsForZSet().score(redisKey, "芬达"));//统计某一项目的分数
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey, "武器"));//统计某一个项目的排名,由大到小
        System.out.println(redisTemplate.opsForZSet().range(redisKey, 0, 2));

    }

    @Test
    public void testKeys() {
        redisTemplate.delete("test:user");

        System.out.println(redisTemplate.hasKey("test:user"));

        redisTemplate.expire("test:students", 10, TimeUnit.SECONDS);//指定过期时效为10s

    }

    //绑定key的对象，
    //多次访问同一个key
    @Test
    public void testBoundOperations() {
        String redisKey = "test:count";
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }

    //redis事务管理比较简单
    //编程式事务
    @Test
    public void testTransaction() {
        Object o = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";

                //启用事务
                operations.multi();

                operations.opsForSet().add(redisKey, "张胜男");
                operations.opsForSet().add(redisKey, "李斯");
                operations.opsForSet().add(redisKey, "汪芜");

                System.out.println(operations.opsForSet().members(redisKey));

                //提交事务
                return operations.exec();
            }
        });
        System.out.println(o);
    }
}
