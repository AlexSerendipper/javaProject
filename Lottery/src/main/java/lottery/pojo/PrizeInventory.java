package lottery.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 @author Alex
 @create 2023-08-26-16:43
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PrizeInventory {
    int prizeId;
    String prizeName;
    int prizeNum;
    String prizeRank;
}
