package community.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * 在服务启动的时候判断是否存在文件存放目录，如果不存在，则创建目录
 @author Alex
 @create 2023-04-28-9:25
 */
@Configuration
public class WkConfig {
    private static final Logger logger = LoggerFactory.getLogger(WkConfig.class);

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @PostConstruct
    public void init(){
        // 创建图片目录
        File file = new File(wkImageStorage);
        if(!file.exists()){
            file.mkdir();
            logger.info("创建wk图片目录：" + wkImageStorage);
        }
    }
}
