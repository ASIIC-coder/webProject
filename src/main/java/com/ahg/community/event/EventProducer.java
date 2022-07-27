package com.ahg.community.event;

import com.ahg.community.entity.Event;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    //处理事件
    //触发事件
    public void fireEvent(Event event) {
        //将事件发布到指定的topic位置
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));//发送一个消息，内容格式为JSON字符串

    }
}
