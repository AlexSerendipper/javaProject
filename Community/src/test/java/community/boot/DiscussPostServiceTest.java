package community.boot;

import com.github.pagehelper.PageInfo;
import community.pojo.DiscussPost;
import community.service.DiscussPostService;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

/**
 @author Alex
 @create 2023-04-03-16:44
 */
@SpringBootTest
public class DiscussPostServiceTest {
    @Autowired
    DiscussPostService discussPostService;

    private static DiscussPost data;

    @BeforeEach
    public void test0(){
        System.out.println("BeforeAll");
        data = new DiscussPost();
        data.setUserId(666);
        data.setTitle("test");
        data.setContent("test content");
        data.setCreateTime(LocalDateTime.now());
        discussPostService.addDiscussPost(data);
    }

    @AfterEach
    public void test1(){
        System.out.println("AfterAll");
        discussPostService.updateStatus(data.getId(),2);
    }

    /**
     * 查询指定用户发布的post
     */
    @Test
    public void test2(){
        PageInfo<DiscussPost> pageInfo = discussPostService.findDiscussPosts(103, 3, 10,0);
        List<DiscussPost> posts = pageInfo.getList();
//        for(DiscussPost post:posts){
//            System.out.println(post);
//        }
    }

    /**
     * 查询某用户发布的post总数
     */
    @Test
    public void test3(){
        int i = discussPostService.countDiscussPosts(103);

//        System.out.println(i);
    }


    /**
     * 测试根据id查询post
     */
    @Test
    public void test4(){
        DiscussPost post = discussPostService.getDiscussPostById(data.getId());
        Assert.assertNotNull(post);
        Assert.assertEquals(data.getTitle(),post.getTitle());
        Assert.assertEquals(data.getContent(),post.getContent());
    }

    /**
     * 修改分数功能：判断影响的行数是否为1
     */
    @Test
    public void test5(){
        int rows = discussPostService.updateScore(data.getId(), 2000);
        Assert.assertEquals(1,rows);
        DiscussPost post = discussPostService.getDiscussPostById(data.getId());
        Assert.assertEquals(post.getScore(),2000,2);  // 精度为2，表示两位小数相等 则二者相等
    }
}
