package community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 @author Alex
 @create 2023-04-05-17:17
 */

@Configuration
public class KaptchaConfig {
    @Bean
    public Producer kaptchaProducer(){
        // Producer是一个Kaptcha的一个接口，他有一个默认实现类DefaultKaptcha
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        // 向DefaultKaptcha中传入config类修改其配置
        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width","100");
        properties.setProperty("kaptcha.image.height","40");
        properties.setProperty("kaptcha.textproducer.font.size","32");
        properties.setProperty("kaptcha.textproducer.font.color","0,0,0");
        // 这里配置的是从哪些字符中生成验证码
        properties.setProperty("kaptcha.textproducer.char.string","0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        // 配置验证码长度
        properties.setProperty("kaptcha.textproducer.char.length","5");
        // 配置验证码的干扰，防止机器人暴力破解，默认不干扰就挺好
        properties.setProperty("kaptcha.noise.impl","com.google.code.kaptcha.impl.NoNoise");

        Config config = new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }
}