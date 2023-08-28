package lottery.util;

import lottery.pojo.User;
import org.springframework.stereotype.Component;

/**
 @author Alex
 @create 2023-04-06-15:15
 */

/**
 * 使用threadlocal持有用户信息User，用于代替session对象，实现数据的线程隔离。提高执行效率 更高效的利用内存,节省开销
 */
@Component
public class HostHolder {
    private ThreadLocal<User> users= new ThreadLocal<>();
    public void setUser(User user){
         users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    /**
     * 为了防止数据冗余
     * 在每次请求结束，清空threadlocal中的数据
     */
    public void clear(){
        users.remove();
    }
}
