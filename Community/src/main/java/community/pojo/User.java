package community.pojo;

import javafx.print.PaperSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 @author Alex
 @create 2023-04-03-20:11
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
// @Slf4j
public class User {
    private Integer id;
    private String username;
    private String password;
    // 用于对用户信息加密，让用户密码加上salt后再进行加密，增强可靠性
    private String salt;
    private String email;
    // 是否为VIP
    private Integer type;
    // 是否激活
    private Integer status;
    // 用户激活码
    private String activationCode;
    // 头像url
    private String headerUrl;
    private Date createTime;
}
