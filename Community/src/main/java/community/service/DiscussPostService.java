package community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import community.mapper.DiscussPostMapper;
import community.pojo.DiscussPost;
import community.util.CommunityConstant;
import community.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 @author Alex
 @create 2023-04-03-16:28
 */
@Service
public class DiscussPostService implements CommunityConstant {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Value("${caffeine.posts.maxsize}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    @Autowired
    private SensitiveFilter sensitiveFilter;


    // 帖子列表缓存
    private LoadingCache<String,PageInfo<DiscussPost>> postListCache;

    // 帖子总数的缓存
    private LoadingCache<Integer,Integer> postCountCache;

    // 只需要在当前类被管理时初始化一次缓存即可，无需多次初始化
    @PostConstruct
    public void init(){
        // 初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)  // 设置最大缓存容量
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)  // 设置缓存存在的时间，在该期间内没有进行读写操作则自动删除缓存
                .build(new CacheLoader<String, PageInfo<DiscussPost>>() {  // 当尝试从缓存中取数据时，若存在数据则正常返回，若不存在相关数据，则需要告诉caffeine如何获取数据，并将其存储到缓存中
                    @Override
                    public @Nullable PageInfo<DiscussPost> load(@NonNull String key) throws Exception {
                        if(key == null || key.length() == 0){
                            throw new IllegalArgumentException("参数错误！");
                        }
                        String [] params = key.split(":");
                        if(params == null || params.length!=2){
                            throw new IllegalArgumentException("参数错误！");
                        }
                        Integer pageNum = Integer.valueOf(params[0]);
                        Integer pageSize = Integer.valueOf(params[1]);
                        PageHelper.startPage(pageNum, pageSize);
                        List<DiscussPost> posts = discussPostMapper.selectDiscussPosts(0,1);
                        // 设置导航栏显示7
                        PageInfo<DiscussPost> pageInfo = new PageInfo<>(posts,7);
                        // 注意，此处在访问数据库之前 可以添加使用二级缓存
                        logger.debug("load posts from DB");  // 若从数据库中取数据时，打印日志
                        return pageInfo;
                    }
                });

        // 初始化帖子总数缓存
        postCountCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds,TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(@NonNull Integer key) throws Exception {
                        return discussPostMapper.countDiscussPosts(key);
                    }
                });
    }

    /**
     * 查询posts
     * @param userId
     * @param pageNum:当前页
     * @param pageSize
     * @return PageInfo<DiscussPost>：返回的即为当前页的相关信息
     */
    public PageInfo<DiscussPost> findDiscussPosts(int userId,int pageNum,int pageSize,int orderMode){
        // 满足serId==0 && orderMode ==1，说明访问的是首页热门帖子，故使用缓存
        if(userId==0 && orderMode ==1){
           return postListCache.get(pageNum + ":" + pageSize);
        }
        PageHelper.startPage(pageNum, pageSize);
        List<DiscussPost> posts = discussPostMapper.selectDiscussPosts(userId,orderMode);
        // 设置导航栏显示7
        PageInfo<DiscussPost> pageInfo = new PageInfo<>(posts,7);
        logger.debug("load posts from DB");  // 若从数据库中取数据时，打印日志
        return pageInfo;
    }

    /**
     * 记录发帖总数
     * @param userId
     * @return
     */
    public int countDiscussPosts(int userId){
        // 当userId=0时，即查询首页时，使用caffeine 将数据缓存，由于必须要传入key，故使用0作为key
        if(userId == 0){
            return postCountCache.get(userId);

        }
        logger.debug("load postCount from DB");
        return discussPostMapper.countDiscussPosts(userId);
    }

    /**
     * 新增帖子
     * @param
     * @return
     */
    public int addDiscussPost(DiscussPost post){
        if(post==null){
            throw new IllegalArgumentException("输入参数不能为空");
        }
        // 转义HTML标记(就是原先title中不能有标签，如<p>，会对页面造成破坏，所有要进行转义)
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        // 过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));
        return discussPostMapper.insertDiscussPost(post);
    }


    /**
     * 根据id获取discusspost
     * @param id
     * @return
     */
    public DiscussPost getDiscussPostById(int id){
        return discussPostMapper.getDiscussPostById(id);
    }


    /**
     * 更新帖子评论数量
     * @param id
     * @param commentCount
     * @return
     */
    public int updateCommentCount(int id,int commentCount){
        return discussPostMapper.updateCommentCount(id,commentCount);
    }


    // 更新帖子类型
    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id, type);
    }

    // 更新帖子状态
    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }

    // 更新帖子分数
    public int updateScore(int id, double score) {
        return discussPostMapper.updateScore(id, score);
    }
}
