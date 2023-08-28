package lottery.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 @author Alex
 @create 2023-04-05-19:46
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
// @Slf4j
public class LoginTicket implements Serializable {
    private int id;
    private int userId;
    private String ticket;
    // 判断当前凭证是否过期
    private int status;
    // 过期时间
    private Date expired;
}
