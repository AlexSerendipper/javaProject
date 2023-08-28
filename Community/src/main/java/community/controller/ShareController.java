package community.controller;

import community.event.EventProducer;
import community.pojo.Event;
import community.util.CommunityConstant;
import community.util.CommunityUtil;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

/**
 @author Alex
 @create 2023-04-28-9:38
 */
@Controller
public class ShareController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);
    @Autowired
    private EventProducer eventProducer;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${wk.image.storage}")
    private String wkImageStorage;
//
    @Value("${wk.image.command}")
    private String wkImageCommand;

//    @Value("${qiniu.bucket.share.url}")
//    private String shareBucketUrl;



    /**
     * 实际上应该要传入不同的功能作为参数，根据不同的功能，生成不同的图片。。。这里为方便，就直接传入不同的url了
     * 调用格式为：http://localhost:8080/community/share?htmlUrl=https://www.nowcoder.com
     * 将生成图片到指定路径并 返回 所生成图片的访问路径
     * @return
     */
    @RequestMapping(path = "/share",method = RequestMethod.GET)
    @ResponseBody
    public String share(String htmlUrl){
        // 文件名
        String fileName = CommunityUtil.generateUUID();
        Event event = new Event()
                .setTopic(TOPIC_SHARE)
                .setData("htmlUrl",htmlUrl)
                .setData("fileName",fileName)
                .setData("suffix",".png");

        eventProducer.fireEvent(event);

        HashMap<String, Object> map = new HashMap<>();
        map.put("shareUrl",domain+contextPath+"/share/image/" + fileName);  // 存储到本地web路径
//        map.put("shareUrl",shareBucketUrl  + "/" + fileName);  // 存储到 七牛云的访问路径
        // 返回分享的图片的访问路径
        return CommunityUtil.getJsonString(0,null, map);
    }


    /**
     * 获取在本地的 生成的 用于分享的图片（这里的访问路径，即为share()方法返回到页面上的路径）
     * 若使用七牛云后 直接废弃该功能即可
     * @param fileName
     * @param resp
     */
    @GetMapping("/share/image/{fileName}")
    public void getShareImage(@PathVariable("fileName") String fileName, HttpServletResponse resp){
        if(StringUtils.isBlank(fileName)){
            throw new IllegalArgumentException("文件名不能为空");
        }
        resp.setContentType("image/png");
        File file = new File(wkImageStorage + "/" + fileName + ".png");
        try {
            ServletOutputStream os = resp.getOutputStream();
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer,0,len);
            }
            logger.info("获取长图成功");

        } catch (IOException e) {
            logger.info("获取长图失败：" + e.getMessage());
        }
    }

}
