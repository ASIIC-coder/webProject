package com.ahg.community.util;


import com.ahg.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户的信息，代替session对象
 */
@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<User>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    public void clear() {
        users.remove();
    }
}
