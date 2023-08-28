package community.controller;

import community.event.EventProducer;
import community.pojo.Comment;
import community.pojo.DiscussPost;
import community.pojo.Event;
import community.pojo.User;
import community.service.LikeService;
import community.util.CommunityConstant;
import community.util.CommunityUtil;
import community.util.HostHolder;
import community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

/**
 @author Alex
 @create 2023-04-11-15:24
 */
@Controller
public class LikeController implements CommunityConstant {
    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType,int entityId,Integer targetUserId,Integer postId){
        User user = hostHolder.getUser();
        // 后续使用 springsecurity 实现权限的管理，这里暂时不判断用户是否登录
        likeService.like(user.getId(),entityType,entityId,targetUserId);
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 用户点赞状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);


        // 触发点赞事件(只有点赞才发通知)
        if(likeStatus==1){
            Event event = new Event();
            event.setTopic(TOPIC_LIKE).setUserId(hostHolder.getUser().getId())
                                    .setEntityType(entityType)
                                    .setEntityId(entityId)
                                    .setEntityUserId(targetUserId)  // 点赞都是给人点赞
                                    .setData("postId",postId);  // 当查看系统发来：有人赞了你的 帖子/评论 要能跳转到对应帖子（回复和讨论也是跳转到帖子）
            eventProducer.fireEvent(event);
        }

        // 对帖子点赞，才重新计算分数
        if(entityType == ENTITY_TYPE_POST){
            // 计算帖子分数
            String postScoreKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(postScoreKey,postId);
        }

        return CommunityUtil.getJsonString(0,"null",map);
    }
}
