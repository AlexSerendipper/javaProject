package lottery.service;


import lottery.mapper.UserMapper;
import lottery.pojo.LoginTicket;
import lottery.pojo.User;
import lottery.util.CommunityConstant;
import lottery.util.CommunityUtil;
import lottery.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 @author Alex
 @create 2023-04-03-20:06
 */
@Service
public class UserService implements CommunityConstant {
    @Autowired
    UserMapper userMapper;

//    @Autowired
//    LoginTicketMapper loginTicketMapper;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * 用户注册功能
     * @param user
     * @return 使用map存储所有错误相关信息
     * @throws Exception
     */
    public Map<String,Object> register(User user) throws Exception {
        // 使用map存储所有错误相关信息
        HashMap<String, Object> map = new HashMap<>();

        // 空值处理
        if(user == null){
            throw new IllegalArgumentException("参数不能为空");
        }

        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空");
            return map;
        }

        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
            return map;
        }

        if(StringUtils.isBlank(user.getUsername())){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }

        // 验证账号、邮箱
//        System.out.println(user.getUsername());
        User userByName = userMapper.getUserByName(user.getUsername());
        User userByEmail = userMapper.getUserByEmail(user.getEmailAddress());

        if(userByName != null){
            map.put("usernameMsg","该账号已存在");
            return map;
        }

        if(userByEmail != null){
            map.put("emailMsg","该邮箱已被注册已存在");
            return map;
        }

        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setCreateTime(new Date());
        user.setEmailAddress(user.getEmailAddress());
        user.setLotteryTimes(3);
        user.setScore(500);
        userMapper.insertUser(user);
        return map;
    }


    /**
     * 用户登录
     * @param username
     * @param password
     * @param expiredSeconds
     * @return map用来存储用户登录返回的信息，登录失败的原因等
     */
    public Map<String,Object> login(String username,String password,int expiredSeconds){
        HashMap<String, Object> map = new HashMap<>();
        // 空值处理
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }

        // 合法性验证：验证账号、状态、密码
        User userByName = userMapper.getUserByName(username);
        if(userByName == null){
            map.put("usernameMsg","该账号不存在！");
            return map;
        }
        password = CommunityUtil.md5(password + userByName.getSalt());
        if(!userByName.getPassword().equals(password)){
            map.put("passwordMsg","密码错误！");
            return map;
        }

        // 登录成功，生成登陆凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(userByName.getUserId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        // 设置的expiredSeconds是秒数，换算成毫秒 然后设置过期时间
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds*1000));

        // 原先将登录凭证存储在数据库中，现将登录凭证存储在redis中
//        int i = loginTicketMapper.insertLoginTicket(loginTicket);
        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey,loginTicket);
        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    /**
     * 用户退出，修改凭证状态
     * @param ticket:用户的登录凭证
     */
    public void logout(String ticket){
//        int i = loginTicketMapper.updateStatus(ticket, 1);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey,loginTicket);
    }

    /**
     * 根据ticket查询登录凭证信息
     * @param ticket
     * @return
     */
    public LoginTicket findLoginTicket(String ticket){
//        return loginTicketMapper.selectByTicket(ticket);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
    }



    /**
     * 修改用户密码
     * @param userId
     * @param newPassword
     * @return：被影响的行数
     */
    public int updateUserPassword(int userId,String newPassword){
//        return userMapper.updatePassword(userId,newPassword);
        int i = userMapper.updatePassword(userId, newPassword);
        clearCache(userId);
        return i;
    }

    public User getUserByName(String username){
        return userMapper.getUserByName(username);
    }

    public User getUserById(Integer id){
//        User user = userMapper.getUserById(id);
        User user = getCache(id);
        if(user==null){
            user = initCache(id);
        }
        return user;
    }

    // 优先从缓存中取值
    public User getCache(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);
        // 直接往redis中存储对象即可，redis会自动存为json字符串
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    // 取不到值时 初始化缓存数据
    public User initCache(int userId){
        User user = userMapper.getUserById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey,user,3600, TimeUnit.SECONDS);
        return user;
    }

    // 数据变更时 清楚缓存数据
    public void clearCache(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }

}
