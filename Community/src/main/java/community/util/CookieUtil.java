package community.util;

/**
 @author Alex
 @create 2023-04-06-15:00
 */

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * 通过request对象获取cookie的小方法封装!!!
 */
public class CookieUtil {
    public static String getCookieValue(HttpServletRequest request,String name){
        if(request == null || name ==null){
            throw new IllegalArgumentException("参数为空！");
        }
        Cookie[] cookies = request.getCookies();
        if(cookies!=null){
            for(Cookie cookie:cookies){
                if(cookie.getName().equals(name)){
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
