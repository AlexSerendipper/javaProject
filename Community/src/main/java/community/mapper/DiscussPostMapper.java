package community.mapper;

import community.pojo.DiscussPost;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 @author Alex
 @create 2023-04-03-14:47
 */
@Mapper
public interface DiscussPostMapper {
    /**
     * 实现查询帖子的功能：
     * 动态sql实现：（ID为0时查询所有数据）（实现查询[我的帖子]的功能时需要传入ID）
     * @param userId
     * @return
     */
    List<DiscussPost> selectDiscussPosts(int userId,int orderMode);

    /**
     * 实现根据ID查询所有帖子记录数
     * 动态sql实现：（ID为0时 查询所有数据）
     * @param userId
     * @return
     */
    int countDiscussPosts(int userId);

    /**
     * 新增帖子功能
     * @param
     * @return
     */
    int insertDiscussPost(DiscussPost discussPost);

    /**
     * 根据id查询帖子详情
     * @param id
     * @return
     */
    DiscussPost getDiscussPostById(int id);

    /**
     * 当添加帖子时，要更新评论的数量
     * @param id
     * @param commentCount
     * @return
     */
    int updateCommentCount(int id,int commentCount);

    // 修改帖子 为指定类型（0普通。1置顶）
    int updateType(int id, int type);

    // 修改帖子 为指定状态（0-正常;1-精华;2-拉黑;）
    int updateStatus(int id, int status);

    // 修改帖子为指定分数
    int updateScore(int id, double score);

}
