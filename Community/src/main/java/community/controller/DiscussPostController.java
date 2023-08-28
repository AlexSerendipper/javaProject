package community.controller;

import com.github.pagehelper.PageInfo;
import community.event.EventProducer;
import community.mapper.UserMapper;
import community.pojo.Comment;
import community.pojo.DiscussPost;
import community.pojo.Event;
import community.pojo.User;
import community.service.CommentService;
import community.service.DiscussPostService;
import community.service.LikeService;
import community.service.UserService;
import community.util.CommunityConstant;
import community.util.CommunityUtil;
import community.util.HostHolder;
import community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 @author Alex
 @create 2023-04-08-10:56
 */
@Controller
@RequestMapping("discuss")
public class DiscussPostController implements CommunityConstant{
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加帖子
     * @param title
     * @param content
     * @return
     */
    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title,String content){
        // 合法性验证
        User user = hostHolder.getUser();
        if(user == null){
            return CommunityUtil.getJsonString(403,"请先登录哦~！");
        }
        // 添加帖子
        DiscussPost post = new DiscussPost( );
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(LocalDateTime.now());
        discussPostService.addDiscussPost(post);

        // 触发发帖事件，存到elasticsearch服务器中
        Event event = new Event().setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);

        // 计算帖子分数,将帖子id存储在set中
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey,post.getId());

        // 报错的情况将来统一处理
        return CommunityUtil.getJsonString(0,"发布成功！");
    }

    /**
     * 查看帖子详情
     * @param discussPostId
     * @param model
     * @return
     */
    @GetMapping(value = {"/detail/{discussPostId}/{pageNum}","/detail/{discussPostId}"})
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, @PathVariable(value = "pageNum",required = false) Integer pageNum, Model model){
        if(pageNum==null){
            pageNum=1;
        }
        // 查看帖子详情页面功能
        DiscussPost post = discussPostService.getDiscussPostById(discussPostId);
        User user = userMapper.getUserById(post.getUserId());
        model.addAttribute("post",post);
        model.addAttribute("user",user);

        // 帖子的点赞信息
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        int likeStatus = hostHolder.getUser()==null?0:likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);  // 判断用户如果没登陆，直接返回0就好了，肯定是
        model.addAttribute("likeCount",likeCount);
        model.addAttribute("likeStatus",likeStatus);

        // 评论的详细信息（这里统一 帖子的comment叫评论，给评论的comment叫回复，给回复的comment叫讨论）
        // 暂时设置每页显示5楼评论
        PageInfo<Comment> commentPageInfo = commentService.getCommentByEntity(ENTITY_TYPE_POST, post.getId(), pageNum, 5);
        List<Comment> comments = commentPageInfo.getList();  // 所有的评论
        List<Map<String,Object>> commentVolist = new ArrayList<>();  // 评论的VO列表
        if(comments!=null){
            for(Comment comment:comments){
                User u = userService.getUserById(comment.getUserId());
                HashMap<String, Object> commentMap = new HashMap<>();  // 存储、每个评论的内容和作者
                commentMap.put("comment",comment);
                commentMap.put("user",u);
                // 评论的点赞信息
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                likeStatus = hostHolder.getUser()==null?0:likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());  // 判断用户如果没登陆，直接返回0就好了，肯定是 未赞
                commentMap.put("likeCount",likeCount);
                commentMap.put("likeStatus",likeStatus);

                // 回复的信息(回复列表暂不支持分页~~~)
                PageInfo<Comment> replyPageInfo = commentService.getCommentByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 1, Integer.MAX_VALUE);
                List<Comment> replys = replyPageInfo.getList();  // 所有的回复
                List<Map<String,Object>> replyVolist = new ArrayList<>();  // 回复的VO列表(这里一定要注意，每条评论都有其自己回复VO列表)
                if(replys!=null){
                    for(Comment reply:replys){
                        User uu = userService.getUserById(reply.getUserId());
                        HashMap<String, Object> replyMap = new HashMap<>();  // 每个回复的内容和作者
                        replyMap.put("reply",reply);
                        replyMap.put("user",uu);
                        // 处理回复的目标！！这里有两种情况，一种是回复！一种讨论（回复的回复）！
                        // 对评论的回复相当于也是发表自己的观点，所以是没有targetId的，而讨论就是针对某个人进行回复，所以是有targetId的。。。。类比于微博！！！！！
                        User uuu = reply.getTargetId() == 0 ? null : userService.getUserById(reply.getTargetId());
                        replyMap.put("target",uuu);

                        // 讨论的点赞信息
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        likeStatus = hostHolder.getUser()==null?0:likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());  // 判断用户如果没登陆，直接返回0就好了，肯定是 未赞
                        replyMap.put("likeCount",likeCount);
                        replyMap.put("likeStatus",likeStatus);
                        replyVolist.add(replyMap);
                    }
                }
                commentMap.put("replys",replyVolist);
                // 讨论的数量！！传入每条评论的id
                int discussCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentMap.put("discussCount",discussCount);
                commentVolist.add(commentMap);
            }
        }
        model.addAttribute("comments",commentVolist);
        model.addAttribute("pageInfo",commentPageInfo);
        model.addAttribute("pageUrl","/discuss/detail/"+discussPostId+"/");
        return "/site/discuss-detail";
    }

    /**
     * 帖子置顶,采用异步的方式实现，置顶后无需刷新页面
     * @param id：帖子id
     * @return
     */
    @PostMapping("/top")
    @ResponseBody
    public String setTop(int id){
        discussPostService.updateType(id,1);
        // 由于帖子发生了变化，故需要更新elasticsearch（重新触发发帖事件）
        // 触发发帖事件，存到elasticsearch服务器中
        Event event = new Event().setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJsonString(0);
    }

    /**
     * 帖子加精,采用异步的方式实现，操作后无需刷新页面
     * @param id：帖子id
     * @return
     */
    @PostMapping("/wonderful")
    @ResponseBody
    public String setWonderful(int id){
        discussPostService.updateStatus(id,1);
        // 由于帖子发生了变化，故需要更新elasticsearch（重新触发发帖事件）
        // 触发发帖事件，存到elasticsearch服务器中
        Event event = new Event().setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        // 计算帖子分数
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey,id);

        return CommunityUtil.getJsonString(0);
    }

    /**
     * 帖子删除,采用异步的方式实现，操作后无需刷新页面
     * @param id：帖子id
     * @return
     */
    @PostMapping("/delete")
    @ResponseBody
    public String setDelete(int id){
        discussPostService.updateStatus(id,2);
        // 由于帖子被删除了，要将其在elasticsearch中删除
        // 触发删帖事件
        Event event = new Event().setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJsonString(0);
    }
}
