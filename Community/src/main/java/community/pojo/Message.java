package community.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 私信类（包括用户的私信和系统的通知）
 @author Alex
 @create 2023-04-09-19:10
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
// @Slf4j
public class Message {
    // 因为id是自动生成 故时间越晚,id越大
    private int id;
    // 消息发送者的id(若fromId=1，说明是系统发送的通知)
    private int fromId;
    // 消息接收者的id
    private int toId;
    // 当前会话id（设计规则：小的id_大的id）（系统消息存储主题即可）
    private String  conversationId;
    private String content;
    // 状态：0表示未读，1表示已读，2表示删除 (默认为0)
    private int status;
    private Date createTime;

}
