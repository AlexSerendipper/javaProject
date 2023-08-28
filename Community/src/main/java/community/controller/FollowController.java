package community.controller;

import community.event.EventProducer;
import community.pojo.Event;
import community.pojo.Page;
import community.pojo.PageInfo;
import community.pojo.User;
import community.service.FollowService;
import community.service.UserService;
import community.util.CommunityConstant;
import community.util.CommunityUtil;
import community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 @author Alex
 @create 2023-04-13-9:02
 */
@Controller
public class FollowController implements CommunityConstant {
    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    @PostMapping("/follow")
    @ResponseBody
    public String follow(int entityType,int entityId){
        User user = hostHolder.getUser();
        followService.follow(user.getId(),entityType,entityId);

        // 触发关注事件(只有关注才发通知)
        Event event = new Event();
        event.setTopic(TOPIC_FOLLOW).setUserId(hostHolder.getUser().getId())
                                    .setEntityType(entityType)
                                    .setEntityId(entityId)
                                    .setEntityUserId(entityId);  // 当查看系统发来：某人关注了你 要能跳转到对应人的详情页面,传入entityId即可，无需设置postId
        eventProducer.fireEvent(event);
        return CommunityUtil.getJsonString(0,"已关注");
    }

    @PostMapping("/unfollow")
    @ResponseBody
    public String unfollow(int entityType,int entityId){
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(),entityType,entityId);
        return CommunityUtil.getJsonString(0,"已取消关注");
    }

    /**
     * 查询用户关注列表
     * @param userId
     * @param pageNum
     * @param model
     * @return
     */
    @GetMapping(value = {"/followees/{userId}/{pageNum}","/followees/{userId}"})
    public String getFollowees(@PathVariable(value = "userId") Integer userId,@PathVariable(value = "pageNum",required = false) Integer pageNum, Model model){
        User user = userService.getUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user",user);

        if(pageNum==null){
            pageNum=1;
        }
        long total = followService.findFolloweeCount(userId,CommunityConstant.ENTITY_TYPE_USER);
        Page page = new Page(pageNum, 5, (int) total);
        // 用户关注列表
        List<Map<String, Object>> followeeList = followService.findFolloweeList(userId, page.getOffset(), page.getPageSize());
        PageInfo pageInfo = new PageInfo(page,5);

        if(followeeList!=null){
            for(Map<String, Object> map:followeeList){
                // 关注列表的用户数据，我们需要判断当前登录用户对这些关注列表的用户是否是 可关注的（因为我们可能会去浏览别人的关注列表）
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }

        model.addAttribute("pageInfo",pageInfo);
        model.addAttribute("followeeList",followeeList);
        model.addAttribute("pageUrl","/followees/"+userId+"/");
        return "/site/followee";
    }

    /**
     * 查询用户粉丝列表
     * @param userId:你点的别人的用户的id
     * @param pageNum
     * @param model
     * @return
     */
    @GetMapping(value = {"/followers/{userId}/{pageNum}","/followers/{userId}"})
    public String getFollowers(@PathVariable("userId") Integer userId,@PathVariable(value = "pageNum",required = false) Integer pageNum, Model model){
        User user = userService.getUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user",user);

        if(pageNum==null){
            pageNum=1;
        }
        long total = followService.findFollowerCount(ENTITY_TYPE_USER,userId);
        Page page = new Page(pageNum, 5, (int) total);
        // 用户关注列表
        List<Map<String, Object>> followerList = followService.findFollowerList(userId, page.getOffset(), page.getPageSize());
        PageInfo pageInfo = new PageInfo(page,5);

        if(followerList!=null){
            for(Map<String, Object> map:followerList){
                // 关注列表的用户数据，我们需要判断当前登录用户对这些关注列表的用户是否是 可关注的（因为我们可能会去浏览别人的关注列表）
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("pageInfo",pageInfo);
        model.addAttribute("followerList",followerList);
        model.addAttribute("pageUrl","/followers/"+userId+"/");
        return "/site/follower";
    }

    /**
     * 判断当前登录用户对 某关注列表 的用户是否是 可关注的
     * @param userId：传入关注列表中，其他用户的id
     * @return
     */
    private boolean hasFollowed(int userId){
        if(hostHolder.getUser()==null){
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(),CommunityConstant.ENTITY_TYPE_USER,userId);
    }
}
