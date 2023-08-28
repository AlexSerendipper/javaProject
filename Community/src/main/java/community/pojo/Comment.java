package community.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 评论、回复、讨论 类
 @author Alex
 @create 2023-04-08-20:48
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
// @Slf4j
public class Comment {
    private int id;
    private int userId;
    // 当前评论目标的类型，对帖子进行评论(entityType=1)，对帖子的回复(entityType=2)，对帖子的讨论(entityType=2)
    private int entityType;
    // 当前评论目标的具体类型 的 id号(如：对帖子进行评论，指向帖子的id号，如对帖子进行回复，则指向评论的id号)
    private int entityId;
    // 如：对某条评论进行回复，具体要指向哪个用户，就使用targetId进行区分
    private int targetId;
    private String content;
    // status=0代表数据有效
    private int status;
    private Date createTime;
}
