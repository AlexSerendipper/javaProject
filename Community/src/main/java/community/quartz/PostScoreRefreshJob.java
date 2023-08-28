package community.quartz;

import community.pojo.DiscussPost;
import community.service.DiscussPostService;
import community.service.ElasticSearchService;
import community.service.LikeService;
import community.util.CommunityConstant;
import community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.Date;

/**
 @author Alex
 @create 2023-04-26-9:40
 */

public class PostScoreRefreshJob implements Job, CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    private static final Date epoch;

    static{
        try {
            epoch=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化时间纪元失败!",e);
        }
    }

    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private ElasticSearchService elasticSearchService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations ops = redisTemplate.boundSetOps(postScoreKey);  // 当时需要更新分数的帖子是以postScoreKey为key， 以postId为值存储在redis中
        if(ops.size()==0){
            logger.info("[任务取消] 没有需要刷新的帖子");
            return;
        }
        logger.info("[任务开始] 正在刷新帖子的分数：" + ops.size());
        while (ops.size() > 0){
            // 每次弹出一个值
            this.refresh((Integer)ops.pop());
        }
        logger.info("[任务结束] 帖子分数刷新完毕!");
    }

    private void refresh(int postId){
        DiscussPost post = discussPostService.getDiscussPostById(postId);
        // 当我们要计算分数时，发现该帖已经被管理员删除了
        if(post == null){
            logger.error("该帖子不存在：id=" + postId);
            return;
        }
        // 是否精华
        boolean wonderful = post.getStatus() == 1;
        // 评论数量
        int commentCount = post.getCommentCount();
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);
        // 计算分数
        double w1 = (wonderful?75:0) + commentCount * 10 + likeCount*2;  // 权重值1
        double w2 = (post.getCreateTime().toInstant(ZoneOffset.of("+8")).toEpochMilli() - epoch.getTime())/(1000*3600*24);  // 权重值2：换算成天数
        // 权重w不能取到小于1的值，否则计算结果会有负数，故取权重和1的最大值
        double score = Math.log10(Math.max(w1,1)) + w2;
        discussPostService.updateScore(postId,score);
        // 同步搜索的数据
        post.setScore(score);
        elasticSearchService.saveDiscusspost(post);
    }



}
