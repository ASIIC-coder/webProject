package com.ahg.community.dao;


import com.ahg.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
@Deprecated//不推荐使用的注解
public interface LoginTicketMapper {

    @Insert({
            "Insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId}, #{ticket}, #{status}, #{expired}) "
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);


    @Select({
            "select id,user_id, ticket,status,expired ",
            "from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);


    @Update({
            "<script> ",
            "update login_ticket set status=#{status} where ticket=#{ticket} ",
            "<if test=\"ticket!=null\"> ",
            "and 1=1 ",
            "</if> ",
            "</script> "
    })
    int updateStatus(String ticket, int status);
}
