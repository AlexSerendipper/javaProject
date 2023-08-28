package community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 @author Alex
 @create 2023-05-02-16:28
 */

@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {}
