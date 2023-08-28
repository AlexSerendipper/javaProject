package lottery.mapper;

import lottery.pojo.RaffleRecords;
import lottery.pojo.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 @author Alex
 @create 2023-04-03-20:14
 */
@Mapper
public interface RaffleRecordMapper {
    int insertRaffleRecord(RaffleRecords raffleRecord);

    List<RaffleRecords> getRaffleRecordByUserId(int userId);
}
