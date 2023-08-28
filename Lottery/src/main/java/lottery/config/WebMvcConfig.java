package lottery.config;

import lottery.interceptor.LoginTicketInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 @author Alex
 @create 2023-04-06-13:16
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    LoginTicketInterceptor loginTicketInterceptor;
//    @Autowired
//    LoginRequiredInterceptor loginRequiredInterceptor;
//
//    @Autowired
//    MessageInterceptor messageInterceptor;
//
//    @Autowired
//    DataInterceptor dataInterceptor;

    // 添加拦截器。指定拦截规则。如果是拦截所有/**，静态资源也会被拦截
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png ","/**/*.jpg","/**/*.jpeg ");  // 放行路径，不能拦截静态资源

//        registry.addInterceptor(loginRequiredInterceptor)
//                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png ","/**/*.jpg","/**/*.jpeg ");  // 放行路径，不能拦截静态资源

//        registry.addInterceptor(messageInterceptor)
//                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png ","/**/*.jpg","/**/*.jpeg ");  // 放行路径，不能拦截静态资源
//
//        registry.addInterceptor(dataInterceptor)
//                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png ","/**/*.jpg","/**/*.jpeg ");  // 放行路径，不能拦截静态资源
    }





}
