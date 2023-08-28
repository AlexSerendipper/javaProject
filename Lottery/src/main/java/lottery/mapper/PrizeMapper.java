package lottery.mapper;

import lottery.pojo.PrizeInventory;
import lottery.pojo.User;
import org.apache.ibatis.annotations.Mapper;

/**
 @author Alex
 @create 2023-04-03-20:14
 */
@Mapper
public interface PrizeMapper {
   /**
    * 根据id查询礼品
    * @return
    */
   PrizeInventory getPrizeById(int prizeId);

   /**
    * 根据礼品名查询礼品
    * @return
    */
   PrizeInventory getPrizeByName(String prizeName);
}
