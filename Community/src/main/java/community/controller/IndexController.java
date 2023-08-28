package community.controller;

import com.github.pagehelper.PageInfo;
import community.pojo.DiscussPost;
import community.pojo.User;
import community.service.DiscussPostService;
import community.service.LikeService;
import community.service.UserService;
import community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 @author Alex
 @create 2023-04-03-21:02
 */
@Controller
public class IndexController implements CommunityConstant{
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    /**
     * 返回首页帖子列表，当ordermode=0时为默认的时间倒叙，当ordermode=1时为热门排序
     * @param model
     * @param pageNum
     * @param orderMode
     * @return
     */
    @GetMapping(value = {"/index/{orderMode}/{pageNum}","/index/{orderMode}","/index","/"})
    public String getIndexPage (Model model, @PathVariable(value = "pageNum",required = false) Integer pageNum, @PathVariable(name = "orderMode",required = false) Integer orderMode){
        if(pageNum==null){
            pageNum=1;
        }

        if(orderMode==null){
            orderMode=0;
        }

        // 设置每页显示条数pageSize
        PageInfo<DiscussPost> pageInfo = discussPostService.findDiscussPosts(0, pageNum, 10, orderMode);

        List<DiscussPost> discussPosts = pageInfo.getList();
        List<Map<String,Object>> posts = new ArrayList<>();
        if(discussPosts!=null){
            for(DiscussPost p:discussPosts){
                // 为增强客户体验，查询出posts后也要显示每个Post对应的User
                User user = userService.getUserById(p.getUserId());
                HashMap<String, Object> map = new HashMap<>();
                map.put("post",p);
                map.put("user",user);

                // 查询点赞信息
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, p.getId());
                map.put("likeCount",likeCount);

                posts.add(map);
            }
        }
        // 返回查询到的posts已经对应的user
        model.addAttribute("posts",posts);
        // 返回分页相关数据
        model.addAttribute("pageInfo",pageInfo);
        // 返回 pageUrl，用于分页页面的复用
        model.addAttribute("pageUrl","/index/" + orderMode + "/");
        model.addAttribute("orderMode",orderMode);
        return "index";
    }

    /**
     * 访问错误页面，用于统一处理异常时，重定向到该页面
     * @return
     */
    @GetMapping("/error")
    public String getErrorPage(){
        return "/error/500";
    }
}
