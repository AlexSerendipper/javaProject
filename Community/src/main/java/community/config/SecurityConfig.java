package community.config;

import community.util.CommunityConstant;
import community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 @author Alex
 @create 2023-04-21-20:40
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {
    // 不拦截静态资源~主要就是访问resources下的静态资源时（图片等），就不需要输入用户名和密码了
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }


    /**
     * 主要处理业务的授权功能：底层代码就是默认实现了 所有请求都需要授权（是否登录成功 / 是否有管理员权限）才能访问
     *
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // spring security底层 默认执行了 http.formLogin() 以及 http.logout() 的配置，我们需要覆盖它，才能执行我们自己的登录退出代码
        http.logout().logoutUrl("/springsecuritylogout");

        // 授权相关配置
        http.authorizeRequests()
                .antMatchers("/user/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/conversation/**",
                        "/message/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow")
                .hasAnyAuthority(AUTHORITY_USER,AUTHORITY_ADMIN,AUTHORITY_MODERATOR)  // 表示拥有user或admin权限，都可以访问私信页面
                .antMatchers("/discuss/top","/discuss/wondeful").hasAnyAuthority(AUTHORITY_MODERATOR)  // 置顶、加精只有MODERATOR才能访问
                .antMatchers("/discuss/delete","/data/**","/actuator/**").hasAnyAuthority(AUTHORITY_ADMIN)  // 查看后台信息以及删帖功能只有ADMIN才能访问
                .and()
                .csrf().disable()
                .exceptionHandling()
                    .authenticationEntryPoint(new AuthenticationEntryPoint() {  // 指定当用户没有登录时的处理
                        @Override
                        public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException e) throws IOException, ServletException {
                            String xRequestedWith = req.getHeader("x-requested-with");   // 获取xRequestedWith
                            if("XMLHttpRequest".equals(xRequestedWith)) {
                                // 这里是ajax中收到的数据类型，我们设置了为json类型，所以这里返回json
                                res.setContentType("application/json; charset=UTF-8");
                                PrintWriter writer = res.getWriter();
                                writer.write(CommunityUtil.getJsonString(403,"用户未登录！"));
                            }else {
                                res.sendRedirect(req.getContextPath()+"/login");  // 重定向
                            }
                        }
                    })
                    .accessDeniedHandler(new AccessDeniedHandler() {  // 指定当权限不匹配时 的处理
                        @Override
                        public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException e) throws IOException, ServletException {
                            String xRequestedWith = req.getHeader("x-requested-with");   // 获取xRequestedWith
                            if("XMLHttpRequest".equals(xRequestedWith)) {
                                // 这里是ajax中收到的数据类型，我们设置了为json类型，所以这里返回json
                                res.setContentType("application/json; charset=UTF-8");
                                PrintWriter writer = res.getWriter();
                                writer.write(CommunityUtil.getJsonString(403,"你没有访问此功能的权限！"));
                            }else {
                                res.sendRedirect(req.getContextPath()+"/denied");
                            }
                        }
                    });

//        // 添加filter 处理验证码功能 (指定在判断用户名密码的filter执行之前处理，即UsernamePasswordAuthenticationFilter之前)
//        http.addFilterBefore(new Filter() {
//            @Override
//            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//                HttpServletRequest req = (HttpServletRequest) servletRequest;
//                HttpServletResponse res = (HttpServletResponse) servletResponse;
//                // 只有登录业务才需要执行处理验证码的功能
//                if(req.getServletPath().equals("/login")){
//                    String verifyCode = req.getParameter("verifyCode");
//                    if(verifyCode==null || !verifyCode.equals("1234")){
//                        req.setAttribute("error","验证码错误");
//                        req.getRequestDispatcher("/loginpage").forward(req,res);
//                        return;
//                    }
//                }
//                // 不是登录请求，或者验证码正确，放行！
//                filterChain.doFilter(req,res);
//            }
//        }, UsernamePasswordAuthenticationFilter.class);

//        // 记住我功能
//        http.rememberMe()
//                .tokenRepository(new InMemoryTokenRepositoryImpl())  // 将用户存储在内存中
//                .tokenValiditySeconds(3600*24)  // 过期时间（s）
//                .userDetailsService(userService);  // 实际上底层只是记住了用户name。需要传入userDetailsService，下次可以根据name查询到用户完整信息，从而自动通过认证
    };



}
