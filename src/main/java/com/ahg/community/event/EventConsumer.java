package com.ahg.community.event;


import com.ahg.community.entity.Event;
import com.ahg.community.entity.Message;
import com.ahg.community.service.MessageService;
import com.ahg.community.util.CommunityConstant;
import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {

    //记录消费事件的日志
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    //向message 发送消息
    @Autowired
    private MessageService messageService;

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空");
        }

        //字符串对应的类型
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误");
            return;
        }

        //发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USE_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        //显示事件的内容需要用到的属性值
        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());//得到触发事件的userId
        content.put("entityType", event.getEntityType());//得到触发事件类型--给...点赞、评论、关注
        content.put("entityId", event.getEntityId());//得到事件的实体Id

        if (!event.getData().isEmpty()) {             //遍历一个key-value集合
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                //将key-value中的得到的属性值添加到内容中
                content.put(entry.getKey(), entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }
}
