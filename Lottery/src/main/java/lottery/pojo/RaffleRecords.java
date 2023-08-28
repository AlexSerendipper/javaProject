package lottery.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 @author Alex
 @create 2023-08-26-16:43
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RaffleRecords {
    String raffleId;
    int userId;
    int prizeId;
    Date prizeTime;
}
