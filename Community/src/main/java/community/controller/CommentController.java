package community.controller;

import community.event.EventProducer;
import community.pojo.Comment;
import community.pojo.DiscussPost;
import community.pojo.Event;
import community.service.CommentService;
import community.service.DiscussPostService;
import community.util.CommunityConstant;
import community.util.HostHolder;
import community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 @author Alex
 @create 2023-04-09-15:50
 */
@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {
    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增评论、回复、讨论
     * @param discussPostId
     * @param comment
     * @return
     */
    @PostMapping("/add/{discussPostId}")
    public String addComment(@PathVariable(value = "discussPostId") int discussPostId, Comment comment){
        // 用户评论时只传入三个数据,comment不完整，需要手动补充完整
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);


        // 触发评论事件
        Event event = new Event();
        event.setTopic(TOPIC_COMMENT).setUserId(hostHolder.getUser().getId())
                                    .setEntityType(comment.getEntityType())
                                    .setEntityId(comment.getEntityId())
                                    .setData("postId",discussPostId);  // 当查看系统发来：有来给你的帖子评论，需要跳转到当前帖子

        // 触发 回复/讨论 事件
        // 若新增的是'评论'，则entityUserId传入的是帖子的发布者的userId
        // 若新增的是'回复' 或 '讨论'，则entityUserId传入的是评论者的userId
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            DiscussPost post = discussPostService.getDiscussPostById(comment.getEntityId());  // 此时comment的entityId是帖子ID
            event.setEntityUserId(post.getUserId());
        }else {
            Comment target = commentService.getCommentById(comment.getEntityId());  // 此时comment的entityId是评论的ID，此时就是根据评论id找到具体的评论，然后找到评论的发布者
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);

        // 如果对帖子进行评论，帖子的评论数量将发生变化，所以要重新触发 发帖 事件，以便更新帖子评论数
        // 并且只有当对帖子进行评论，才计算分数
        if(comment.getEntityType() == ENTITY_TYPE_POST){
             new Event().setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);

            // 计算帖子分数
            String postScoreKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(postScoreKey,discussPostId);
        }


        return "redirect:/discuss/detail/" + discussPostId;
    }

}
