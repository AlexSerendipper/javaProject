package community.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;


/**
 * 帖子对应的实体类
 @author Alex
 @create 2023-04-03-14:39
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Document(indexName = "discusspost",shards = 6,replicas = 3)
// @Slf4j
public class DiscussPost {
    @Id
    private int id;
    @Field(type = FieldType.Integer)
    private int userId;
    @Field(type = FieldType.Text,analyzer = "ik_max_word",searchAnalyzer = "ik_smart")
    private String title;
    @Field(type = FieldType.Text,analyzer = "ik_max_word",searchAnalyzer = "ik_smart")
    private String content;
    @Field(type = FieldType.Integer)
    // 0普通。1置顶
    private int type;
    @Field(type = FieldType.Integer)
    // 0-正常;1-精华;2-拉黑;
    private int status;
    @Field(type = FieldType.Date,format = DateFormat.date_hour_minute)
    private LocalDateTime createTime;
    // 每个帖子都记录了 当前帖子的回复数量
    @Field(type = FieldType.Integer)
    private int commentCount;
    @Field(type = FieldType.Double)
    private double score;
}
