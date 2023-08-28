package community.controller;


import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import community.annotation.LoginRequired;
import community.mapper.LoginTicketMapper;
import community.pojo.LoginTicket;
import community.pojo.User;
import community.service.FollowService;
import community.service.LikeService;
import community.service.UserService;
import community.util.CommunityConstant;
import community.util.CommunityUtil;
import community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 @author Alex
 @create 2023-04-07-10:07
 */
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Value("${community.path.upload}")
    private String uploadPath;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    UserService userService;
    @Autowired
    HostHolder hostHolder;
    @Autowired
    LoginTicketMapper loginTicketMapper;

    @Autowired
    LikeService likeService;

    @Autowired
    FollowService followService;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;


    /**
     * 返回修改页面（修改用户头像 以及 修改用户密码 页面）
     * @return
     */
    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage(Model model){
//        // 上传文件的名字
//        String fileName = CommunityUtil.generateUUID();
//        // 设置响应信息（七牛云特定的stringMap格式）
//        StringMap policy = new StringMap();
//        policy.put("returnBody", CommunityUtil.getJsonString(0));
//        // 生成上传凭证
//        Auth auth = Auth.create(accessKey,secretKey);
//        String uploadToken = auth.uploadToken(headerBucketName,fileName,3600,policy);
//        model.addAttribute("uploadToken",uploadToken);
//        model.addAttribute("fileName",fileName);
        return "/site/setting";
    }

    /**
     * 更新头像的web路径
     * @return
     */
    @PostMapping("/header/url")
    @ResponseBody
    public String updateHeaderUrl(String fileName){
        if (StringUtils.isBlank(fileName)) {
            return CommunityUtil.getJsonString(1,"文件名不能为空");
        }

        // 文件在七牛云上的访问路径
        String url = headerBucketUrl + "/" + fileName;
        userService.updateUserHeaderUrl(hostHolder.getUser().getId(),url);
        return CommunityUtil.getJsonString(0,"更新用户头像路径成功");
    }

    /**
     * 上传头像功能: 即更新了头像的web路径
     * 使用云服务器则作废！！！!
     * @param icon
     * @param model
     * @return
     */
    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile icon, Model model){
        if(icon == null){
            model.addAttribute("error","您还没有选择图片！");
            return "forward:/setting";
        }
        // 获取上传的文件的文件名
        String fileName = icon.getOriginalFilename();

        // 处理上传文件重名的问题（通过lastindexof获取上传文件的后缀，UUID为32为随机字符）
        String hzName = fileName.substring(fileName.lastIndexOf("."));
        if(StringUtils.isBlank(hzName)){
            model.addAttribute("error","文件格式不正确！");
            return "forward:/setting";
        }

        // 动态创建存放图片的目录
        if (!new File(uploadPath).exists()) {
            new File(uploadPath).mkdir();
        }

        // 生成文件
        fileName = CommunityUtil.generateUUID() + hzName;
        File file = new File(uploadPath + "/" + fileName);

        try {
            icon.transferTo(file);
        } catch (IOException e) {
            logger.error("上传文件失败"+e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发出异常",e);
        }

        // 更新用户用户头像的路径（肯定是要提供的是，如下web路径）
        // http://localhost:8080/community/user/icon/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/icon/" + fileName;
        userService.updateUserHeaderUrl(user.getId(),headerUrl);
        return "redirect:/index";
    }

    /**
     * 获取用户头像，实际上就是根据用户头像的web路径，找到头像在服务器存储的路径：使用云服务器则作废！！！！
     * @param fileName
     * @param response
     */
    @GetMapping("/icon/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        // 找到头像在服务器存储的地方
        fileName = uploadPath + "/" + fileName;
        // 通过lastindexof获取上传的后缀
        String hzName = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + hzName);
        FileInputStream fis = null;
        try {
            ServletOutputStream os = response.getOutputStream();
            fis = new FileInputStream(fileName);
            byte[] buffer = new byte[1024];
            int len;
            while((len=fis.read(buffer))!=-1){
                os.write(buffer,0,len);
            }
        } catch (IOException e) {
            logger.error("读取头像失败" + e.getMessage());
        }finally {
            // 在springboot中，只有自己创建的流是需要关闭的，其他的springboot都会帮你关闭
            try {
                fis.close();
            } catch (IOException e) {
                logger.error("输入流关闭失败");
            }
        }
    }

    /**
     * 修改密码功能
     * @param oldPassword
     * @param newPassword
     * @param confirmPassword
     * @param model
     * @return
     */
    @GetMapping("/password")
    public String changePassword(String oldPassword,String newPassword,String confirmPassword,Model model){
        User user = hostHolder.getUser();
        String oldRealPassword = CommunityUtil.md5(oldPassword+user.getSalt());
        String newRealPassword = CommunityUtil.md5(newPassword+user.getSalt());
        // 合法性验证
        if(oldPassword.length()<6){
            model.addAttribute("oldMsg","原密码格式错误，长度不能小于6位");
            return "/site/setting";
        }
        if(!oldRealPassword.equals(user.getPassword())){
            model.addAttribute("oldMsg","原密码错误，请重新输入");
            return "/site/setting";
        }
        if(newPassword.length()<6){
            model.addAttribute("newMsg","新密码格式错误，长度不能小于6位");
            return "/site/setting";
        }
        if(!newPassword.equals(confirmPassword)){
            model.addAttribute("confirmMsg","新密码确认失败，请再次核对新密码！");
            return "/site/setting";
        }
        // 更新用户密码 并退出登录
        userService.updateUserPassword(user.getId(),newRealPassword);
        // 验证当前用户的有效凭证（可能一个用户存了多个凭证进来,但是有效且没过期的只有一个）
        List<String> tickets = loginTicketMapper.getTicketByUserId(user.getId());
        for(String ticket:tickets){
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            // 凭证有效性检查
            if (loginTicket!=null && loginTicket.getExpired().after(new Date())){
                userService.logout(ticket);
            }
        }
        return "redirect:/login";
    }

    /**
     * 查看用户主页功能
     * @param userId
     * @param model
     * @return
     */
    @GetMapping("/profile/{userId}")
    public String getUserProfile(@PathVariable(value = "userId") Integer userId,Model model){
        User user = userService.getUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在");
        }
        // 用户的基本信息
        model.addAttribute("user",user);
        // 点赞数量
        int userLikeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("userLikeCount",userLikeCount);
        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);
        // 是否已关注(若当前没有用户登录，则显示未关注，若当前用户已登录，则判断是否已关注
        boolean hasFollowed = false;
        if(hostHolder.getUser()!=null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);
        return "/site/profile";
    }

}
