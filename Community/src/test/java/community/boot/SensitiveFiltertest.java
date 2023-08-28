package community.boot;

import community.mapper.UserMapper;
import community.pojo.User;
import community.util.CommunityUtil;
import community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 @author Alex
 @create 2023-04-03-16:44
 */
@SpringBootTest
public class SensitiveFiltertest {
    @Autowired
    SensitiveFilter sensitiveFilter;
    @Test
    public void test1(){
        String s = "这里可以赌博和嫖娼哈哈哈哈哈！";
        String s1 = sensitiveFilter.filter(s);
        System.out.println(s1);
    }
    @Test
    public void test2(){
        String s = "这里可以☆赌☆博☆和☆☆嫖☆☆娼☆☆哈哈哈哈哈！";
        String s1 = sensitiveFilter.filter(s);
        System.out.println(s1);
    }


}
