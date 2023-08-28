package community.boot;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import community.pojo.Message;
import community.pojo.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 自用pageInfo，用于redis数据的分页！方便复用主页的分页逻辑
 @author Alex
 @create 2023-04-13-14:39
 */
@SpringBootTest
public class RedisPageInfoTest {
    @Autowired
    RedisTemplate redisTemplate;

    @Test
    public void test1(){
        Page page = new Page(2, 3,4);
        Set<Integer> range = redisTemplate.opsForZSet().reverseRange("followee:113:3", page.getOffset(), page.getOffset()+page.getPageSize()-1);
        community.pojo.PageInfo pageinfo = new community.pojo.PageInfo(page, 5);
        System.out.println(range);
        System.out.println(pageinfo);
    }
}
