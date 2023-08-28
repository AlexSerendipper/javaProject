package community.service;

import community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 @author Alex
 @create 2023-04-11-14:47
 */
@Service
public class LikeService {
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 点赞功能，由于是用户点赞功能+更新用户被点赞数功能，两个更新操作，所以使用redis事务
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void like(int userId,int entityType,int entityId,Integer targetUserId){
//        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
//        // 判断用户是否点过赞
//        Boolean islike = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
//        if(islike){
//            redisTemplate.opsForSet().remove(entityLikeKey,userId);
//        }else {
//            redisTemplate.opsForSet().add(entityLikeKey,userId);
//        }

        // redis事务支持
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                // 某个 实体的赞 的key 如： like:entity:entityType --> entityId
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
                // 生成某个 用户的赞 的key。注意使用的是目标用户的UserId，代表给谁点赞（用于记录用户被赞的数量）
                String userLikeKey = RedisKeyUtil.getUserLikeKey(targetUserId);
                // 判断用户是否点过赞
                Boolean islike = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
                redisOperations.multi();  // 开启事务
                    if(islike){  // 如果点过赞
                        redisOperations.opsForSet().remove(entityLikeKey,userId);
                        redisOperations.opsForValue().decrement(userLikeKey);

                    }else {
                        redisOperations.opsForSet().add(entityLikeKey,userId);
                        redisOperations.opsForValue().increment(userLikeKey);
                    }
                return redisOperations.exec();
            }
        });
    }

    /**
     * 查询某实体的点赞数量
     * @param entityType
     * @param entityId
     * @return
     */
    public long findEntityLikeCount(int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }


    /**
     * 查询某人对某实体的点赞状态（是否点过赞）
     * @param entityType
     * @param entityId
     * @return
     */
    public int findEntityLikeStatus(int userId,int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        // 判断用户是否点过赞,这里返回整数（1代表已点赞），方便后续业务扩展（可能还会有踩之类的功能）
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }

    /**
     * 查询某个用户被赞的数量
     * @param UserId
     * @return
     */
    public int findUserLikeCount(int UserId){
        String userLikeKey = RedisKeyUtil.getUserLikeKey(UserId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 :count.intValue();
    }
}
