package community.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.swing.text.html.ObjectView;
import java.util.HashMap;
import java.util.Map;

/**
 * 事件对象
 @author Alex
 @create 2023-04-14-14:00
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
// @Slf4j
public class Event {
    // 事件主题
    private String topic;
    // 事件触发者ID
    private int userId;

    // 事件的类型和id（如果是帖子事件，类型即为帖子，id为帖子id）
    private int entityType;
    private int entityId;

    // 对方的id（如果是给对方的帖子点赞，这就是对方用户的Id）
    private int entityUserId;
    // 对于一些其他的数据 封装到map集合中方便后续的扩展
    private Map<String,Object> data = new HashMap<>();

    // 对一些set方法进行改造，返回当前对象，这样就可以进行链式调用，如xx.setTopic().setuserId()
    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    // 改造，传入key value即可
    public Event setData(String key, Object value) {
        this.data.put(key,value);
        return this;
    }
}
