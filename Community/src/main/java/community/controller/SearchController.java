package community.controller;

import community.pojo.DiscussPost;
import community.pojo.Page;
import community.pojo.PageInfo;
import community.service.ElasticSearchService;
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
 @create 2023-04-20-10:58
 */

// elasticSearch搜索controller
@Controller
public class SearchController implements CommunityConstant {
    @Autowired
    private ElasticSearchService elasticSearchService;
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;

    // search?keyword=
    @GetMapping(value = {"/search/{keyword}/{pageNum}","/search/{keyword}"})
    public String search(@PathVariable("keyword") String keyword, Model model, @PathVariable(value = "pageNum",required = false) Integer pageNum){
        if(pageNum==null){
            pageNum=1;
        }

        // 查询总记录数，其实可以再写一个方法，但感觉也麻烦 算了
        Page page = new Page(pageNum, 10, elasticSearchService.searchDiscusspostCount(keyword));
        PageInfo pageInfo = new PageInfo(page, 5);
        // 查询返回 分页 高亮discusspost结果
        List<DiscussPost> posts = elasticSearchService.searchDiscusspost(keyword, pageNum - 1, page.getPageSize());

        List<Map<String,Object>> searchPosts = new ArrayList<>();
        if(posts!=null){
            for(DiscussPost post:posts){
                HashMap<String, Object> map = new HashMap<>();
                // 帖子
                map.put("post",post);
                // 作者
                map.put("user",userService.getUserById(post.getUserId()));
                // 点赞数量
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId()));
                searchPosts.add(map);
            }
        }

        model.addAttribute("searchPosts",searchPosts);
        model.addAttribute("keyword",keyword);
        model.addAttribute("pageInfo",pageInfo);
        model.addAttribute("pageUrl","/search/" + keyword + "/");
        return "/site/search";
    }

}
