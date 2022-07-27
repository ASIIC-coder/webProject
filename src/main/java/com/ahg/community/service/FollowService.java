package com.ahg.community.service;

import com.ahg.community.entity.User;
import com.ahg.community.util.CommunityConstant;
import com.ahg.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    //取消关注业务方法
    public void unfollow(int userId, int entityType, int entityId) {//传入用户Id,关注的某个实体三个参数
        redisTemplate.execute(new SessionCallback() {//构造redis的事务管理,因为在存储键值对时，需要存两份（对）数据 ：关注的目标和实体的粉丝
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();

                //注意这里存储方法的传入参数，和获得key是对称的
                operations.opsForZSet().remove(followeeKey, entityId);//存储 key(关注目标)--- value(’key‘-（实体Id）)
                operations.opsForZSet().remove(followerKey, userId);//存储 key(实体粉丝） --- value('key’-userid)
                return operations.exec();
            }
        });
    }

    public void follow(int userId, int entityType, int entityId) {//传入用户Id,关注的某个实体三个参数
        redisTemplate.execute(new SessionCallback() {//构造redis的事务管理,因为在存储键值对时，需要存两份（对）数据 ：关注的目标和实体的粉丝
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();

                //注意这里存储方法的传入参数，和获得key是对称的
                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());//存储 key(关注目标)--- value(’key‘-（实体Id）,分数(当前时间))
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());//存储 key(实体粉丝） --- value('key’-userid, 分数)
                return operations.exec();
            }
        });
    }

    //查询关注实体的数量
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);

    }

    //查询实体粉丝的数量
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    //查询当前用户是否已关注该实体
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;

    }

    //查询某个用户关注的人
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        if (targetIds == null) {
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);

        }
        return list;
    }

    //查询某用户的粉丝
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        if (targetIds == null) {
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);

        }
        return list;

    }
}
