package community.controller;

import com.google.code.kaptcha.Producer;
import community.pojo.User;
import community.service.UserService;
import community.util.CommunityConstant;
import community.util.CommunityUtil;
import community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 @author Alex
 @create 2023-04-05-9:43
 */
@Controller
public class LoginController implements CommunityConstant {
    @Autowired
    private UserService userService;
    @Autowired
    private Producer kaptchaProducer;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 访问注册页面
     * @return
     */
    @GetMapping("/register")
    public String getRegistPage(){
        return "/site/register";
    }


    /**
     * 注册
     * @param model
     * @param user
     * @return
     * @throws Exception
     */
    @PostMapping("/register")
    public String Regist(Model model, User user) throws Exception {
        // 在springmvc中传入model和另一对象，另一对象会直接添加到model中！
        Map<String, Object> map = userService.register(user);
        if(map==null || map.isEmpty()){
            // 注册成功后跳到响应页面，并提示注册成功信息
            model.addAttribute("msg","注册成功，我们已经向您的邮箱发送了激活邮件，请尽快激活！");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else{
            // 注册失败仍跳转回注册页面，回显错误消息
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }
    }

    /**
     * 激活
     * @param model
     * @param userId
     * @param activationCode
     * @return
     */
    // http://localhost:8080/community/activation/156/b6e06f032c3d411d879ade679a12d01e
    @GetMapping("activation/{userId}/{activationCode}")
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("activationCode") String activationCode){
        int activationStatus = userService.activation(userId, activationCode);
        if(activationStatus == ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功，您的账号已经可以正常使用了！");
            model.addAttribute("target","/login");
        }else if(activationStatus==ACTIVATION_REPEAT){
            model.addAttribute("msg","无效操作，该账号已经被激活过了！");
            model.addAttribute("target","/index");
        }else {
            model.addAttribute("msg","激活失败，可能是由于激活码失效或不正确导致~");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

    /**
     * 登陆页面
     * @return
     */
    @GetMapping("/login")
    public String getLoginPage(){
        return "/site/login";
    }

    /**
     * 生成验证码
     * @param response
     * @param session:原先使用session存储验证码，重构为使用redis存储
     * @throws Exception
     */
    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) throws Exception{
        // 生成验证码
        String text = kaptchaProducer.createText();
        // 生成图片
        BufferedImage image = kaptchaProducer.createImage(text);
        // 存入session
//        session.setAttribute("kaptchaText",text);

        // 将验证码存入redis (先生成一个owner用来标识当前未登录的账户)
        String owner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("owner",owner);  // 将owner信息存储到cookie中
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(owner);  // 使用owner信息生成Kaptcha的Key
        redisTemplate.opsForValue().set(kaptchaKey,text,60, TimeUnit.SECONDS);  // 将验证码存到redis中，并设置过期时间为60s

        // 将图片输出给浏览器,指定输出的格式
        response.setContentType("image/png");
        ServletOutputStream os = response.getOutputStream();
        // 使用java自带的图片输出工具，指定输出的图片，类型，以及流
        ImageIO.write(image,"png",os);
    }

    /**
     * 用户登录
     * @param username
     * @param password
     * @param code
     * @param rememberMe
     * @param model
     * @param session:原先使用session存储验证码，重构为使用redis存储
     * @param response
     * @return
     */
    @PostMapping("/login")
    public String login(String username, String password, String code, boolean rememberMe,Model model,/*HttpSession session,*/HttpServletResponse response, @CookieValue("owner") String owner){
        // 先判断验证码
//        String kaptcha = (String) session.getAttribute("kaptchaText");
        String kaptcha = null;
        if(StringUtils.isNotBlank(owner)){
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(owner);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }

        if(StringUtils.isBlank(kaptcha) || !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","验证码不正确");
            return "/site/login";
        }

        // 检查账号密码(若勾选记住我，增加保存时间)
        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if(map.containsKey("ticket")){
            // 将登录凭证以cookie的方式发送给客户端存储
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @GetMapping("/logout")
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

    // 用户无权限的访问页面
    @GetMapping("/denied")
    public String getDeniedPage(){
        return "/error/404";
    }
}

