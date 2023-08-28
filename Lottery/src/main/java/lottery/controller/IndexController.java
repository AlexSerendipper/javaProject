package lottery.controller;

import com.github.pagehelper.PageInfo;
import lottery.mapper.UserMapper;
import lottery.pojo.User;
import lottery.service.UserService;
import lottery.util.HostHolder;
import lottery.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
 @create 2023-08-26-15:42
 */

@Controller
public class IndexController {
    @Autowired
    HostHolder hostHolder;
    @Autowired
    UserMapper userMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @GetMapping(value = {"/index","/"})
    public String getIndexPage (Model model){
        User user = hostHolder.getUser();
        if(user==null){
            return "index";
        }
        User userById = userMapper.getUserById(user.getUserId());
        model.addAttribute("user",userById);
        int lotteryTimes = (int) redisTemplate.opsForValue().get(RedisKeyUtil.getLotteryTimesKey(hostHolder.getUser().getUserId(), hostHolder.getUser().getUsername()));
        int score = (int) redisTemplate.opsForValue().get(RedisKeyUtil.getScore(hostHolder.getUser().getUserId(), hostHolder.getUser().getUsername()));
        model.addAttribute("lotteryTimes",lotteryTimes);
        model.addAttribute("score",score);
        return "index";
    }
}
