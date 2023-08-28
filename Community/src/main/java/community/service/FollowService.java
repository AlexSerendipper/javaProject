package community.service;

import com.github.pagehelper.PageInfo;
import community.pojo.User;
import community.util.CommunityConstant;
import community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 @author Alex
 @create 2023-04-12-16:05
 */
@Service
public class FollowService implements CommunityConstant {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;
    /**
     * 关注业务，由于需要存储两份数据，一份是 你的关注+1，一份是 被关注者粉丝+1
     * @param userId：当前用户Id
     * @param entityType:当前关注的实体类型，可以是用户、帖子等
     * @param entityId：当前关注的实体的id
     */
    public void follow(int userId,int entityType,int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                // 如： followee:userId:entityType --> entityId。。。当前用户 关注类型 及其具体ID（方便查询某人关注实体个数）
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
                // 如： follower:entityType:entityId --> userId。。。当前被关注实体类型 具体ID 及关注者ID（方便查询某实体关注者个数）
                String followerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);
                redisOperations.multi();
                    // 将 被关注者 数据存储在zset中，以 entityType 作为值(关注对象类型)，以时间作为分数来排序（后续查看你的粉丝/你的关注时，可以按时间顺序进行排序）
                    redisOperations.opsForZSet().add(followeeKey,entityId, System.currentTimeMillis());
                    // 将 关注者（粉丝） 数据存储在zset中，以 userId 作为值（粉丝ID），以时间作为分数来排序（后续查看你的粉丝/你的关注时，可以按时间顺序进行排序）
                    redisOperations.opsForZSet().add(followerKey,userId, System.currentTimeMillis());
                return redisOperations.exec();
            }
        });
    }

    /**
     * 取消关注业务
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void unfollow(int userId,int entityType,int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);
                redisOperations.multi();
                    redisOperations.opsForZSet().remove(followeeKey,entityId);
                    redisOperations.opsForZSet().remove(followerKey,userId);
                return redisOperations.exec();
            }
        });
    }

    /**
     * 查询你关注的实体的数量
     * @return
     */
    public long findFolloweeCount(int userId,int entityType){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }


    /**
     * 查询某实体的粉丝的数量
     * @return
     */
    public long findFollowerCount(int entityType,int entityId){
        String followerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    /**
     * 查询当前用户是否已关注该实体
     * @param userId:这里传入的是当前用户id，通过查询 当前用户 的关注的key！查询是否已关注
     * @param entityType
     * @param entityId：你所关注的实体的id
     * @return
     */
    public boolean hasFollowed(int userId,int entityType,int entityId){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
        // 查询实体key中是否有entityId（你的关注中是否有该id）,有则返回true
        return redisTemplate.opsForZSet().score(followeeKey,entityId) != null;
    }

    /**
     * 查询某个用户 关注人列表（这里具体到关注人，后续关注其他实体再新增方法）
     * @param userId:当前用户的id
     * @param offset:传入查询开始的索引
     * @param pageSize:传入查询的个数，由于redisTemplate.opsForZSet().reverseRange传入的是开始和结束索引，所以输入offset+limit-1
     * @return
     */
    public List<Map<String,Object>> findFolloweeList(int userId,int offset,int pageSize){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,ENTITY_TYPE_USER);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + pageSize - 1);
        if(targetIds==null){
            return null;
        }
        List<Map<String,Object>> list= new ArrayList<>();
        for(Integer targetId:targetIds){
            HashMap<String, Object> map = new HashMap<>();
            // 查询到我所关注的人
            User user = userService.getUserById(targetId);
            map.put("user",user);
            // 查询到用户关注时间
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }


    /**
     * 查询某个用户的粉丝的列表，这里具体到关注的人
     * @param userId:当前用户的id
     * @param offset:传入查询开始的索引
     * @param pageSize:传入查询的个数，由于redisTemplate.opsForZSet().reverseRange传入的是开始和结束索引，所以输入offset+limit-1
     * @return
     */
    public List<Map<String,Object>> findFollowerList(int userId,int offset,int pageSize){
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER,userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + pageSize - 1);
        if(targetIds==null){
            return null;
        }
        List<Map<String,Object>> list= new ArrayList<>();
        for(Integer targetId:targetIds){
            HashMap<String, Object> map = new HashMap<>();
            // 查询到我的粉丝
            User user = userService.getUserById(targetId);
            map.put("user",user);
            // 查询粉丝关注我的时间
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }
}
