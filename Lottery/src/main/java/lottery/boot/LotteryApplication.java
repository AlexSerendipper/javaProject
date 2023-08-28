package lottery.boot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication(scanBasePackages="lottery")
@MapperScan(value = "lottery.mapper")
//开启扫描搜索引擎的注解
@EnableElasticsearchRepositories(basePackages = "lottery.dao")
public class LotteryApplication {
	public static void main(String[] args) {
		SpringApplication.run(LotteryApplication.class, args);
	}
}
