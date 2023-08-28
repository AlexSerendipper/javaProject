package community.service;

import community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 @author Alex
 @create 2023-04-24-14:48
 */
@Service
public class DataService {
    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    // 将指定的IP计入UV
    public void recordUV(String ip){
        String redisKey = RedisKeyUtil.getUVKey(sdf.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey,ip);
    }

    // 统计指定日期范围内的UV
    public long calculateUV(Date startDate,Date endDate){
        if(startDate==null||endDate==null){
            throw new IllegalArgumentException("参数不能为空");
        }

        // 整理日期范围内的所有key
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);  // 将date类转换为calendar类
        while (!calendar.getTime().after(endDate)){  // calendar类转换为date类
            String key = RedisKeyUtil.getUVKey(sdf.format(calendar.getTime()));
            keyList.add(key);
            calendar.add(Calendar.DATE,1);  // 以日的步进单位，Calendar.DATE返回当前是多少号，每次循环加1，知道遍历出所有的endDate之前的数据
        }

        // 合并数据
        String redisKey = RedisKeyUtil.getUVKey(sdf.format(startDate),sdf.format(endDate));  // 由开始日期和结束日期生成redisKey
        redisTemplate.opsForHyperLogLog().union(redisKey, keyList.toArray());

        // 返回统计的结果
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }


    // 将指定的用户计入DAU
    public void recordDAU(Integer userId){
        String redisKey = RedisKeyUtil.getDAUKey(sdf.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey,userId,true);
    }


    // 统计指定日期范围内的DAU
    // 需要对指定日期范围内的数据进行OR运算（如统计一周内的dau，即只要该用户一周内访问过该网站，则该用户即为活跃用户）
    public long calculateDAU(Date startDate,Date endDate){
        if(startDate==null||endDate==null){
            throw new IllegalArgumentException("参数不能为空");
        }

        // 整理日期范围内的所有key(由于运算时需要传入byte数组的形式，所以提前做好转换)
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        while (!calendar.getTime().after(endDate)){
            String key = RedisKeyUtil.getDAUKey(sdf.format(calendar.getTime()));
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE,1);
        }

       String redisKey=RedisKeyUtil.getDAUKey(sdf.format(startDate),sdf.format(endDate));
       // 合并数据
       Object obj = redisTemplate.execute(new RedisCallback(){
               @Override
               public Object doInRedis(RedisConnection redisConnection) throws DataAccessException{
                   redisConnection.bitOp(RedisStringCommands.BitOperation.OR, redisKey.getBytes(), keyList.toArray(new byte[0][0]));  // 这里为什么转换为二维数组呢？
                   return redisConnection.bitCount(redisKey.getBytes());
               }
        });
        return (long)obj;
    }
}
