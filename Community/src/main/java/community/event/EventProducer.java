package community.event;

import com.alibaba.fastjson.JSONObject;
import community.pojo.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 @author Alex
 @create 2023-04-14-14:17
 */
@Component
public class EventProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;

    // 处理事件（发布）
    public void fireEvent(Event event){
        // 将事件发布到指定的主题，消息内容即为该事件（转换为json格式）
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
