package community.boot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import javax.annotation.PostConstruct;

/**
 @author Alex
 @create 2023-04-03-16:32
 */
@SpringBootApplication(scanBasePackages="community")
@MapperScan(value = "community.mapper")
//开启扫描搜索引擎的注解
@EnableElasticsearchRepositories(basePackages = "community.dao")
public class CommunityApplication {
//    @PostConstruct
//    public void init(){
//        // 解决netty启动冲突问题
//        // see Netty4Utils.setAvailableProcessors()
//        System.setProperty("es.set.netty.runtime.available.processors","false");
//    }
    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
    }
}
