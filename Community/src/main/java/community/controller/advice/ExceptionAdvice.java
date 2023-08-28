package community.controller.advice;

import community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/**
 @author Alex
 @create 2023-04-10-21:16
 */
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {
    private static final Logger logger= LoggerFactory.getLogger(ExceptionAdvice.class);
    // 这里直接处理所有异常
    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest req, HttpServletResponse res) throws IOException {
        logger.error("服务器发生异常" + e.getMessage());
        // 记录详细的错误信息
        for(StackTraceElement element : e.getStackTrace()){
            logger.error(element.toString());
        }

        // 这里要注意，当请求如果是异步请求（ajax时），我们希望返回的是json数据，不再是500的html页面
        String xRequestedWith = req.getHeader("x-requested-with");
        if("XMLHttpRequest".equals(xRequestedWith)){
            // 这里是ajax中收到的数据类型，我们设置了为json类型，所以这里返回json
            res.setContentType("application/json; charset=UTF-8");
            PrintWriter writer = res.getWriter();
            writer.write(CommunityUtil.getJsonString(1,"服务器异常！"));
        }else {
            res.sendRedirect(req.getContextPath()+"/error");
        }
    }
}
