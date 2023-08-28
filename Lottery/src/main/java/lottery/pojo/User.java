package lottery.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 @author Alex
 @create 2023-08-26-16:43
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User implements Serializable {
    int userId;
    String username;
    String password;
    String phoneNumber;
    String emailAddress;
    int score;  // 积分
    String salt;
    int lotteryTimes;  // 收货地址
    Date createTime;
}
