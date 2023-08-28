package community.mapper;

import community.pojo.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 @author Alex
 @create 2023-04-08-20:55
 */
@Mapper
public interface CommentMapper {
    /**
     * 根据entity查询comment
     * @param entityType
     * @param entityId
     * @return
     */
    List<Comment> selectCommentsByEntity(int entityType,int entityId);

    /**
     * 根据id查询comment
     * @return
     */
    Comment selectCommentsById(int id);

    /**
     * 查询评论总数
     * @param entityType
     * @param entityId
     * @return
     */
    int countCommentsByEntity(int entityType,int entityId);

    /**
     * 增加评论
     * @param comment
     * @return
     */
    int insertComment(Comment comment);


}
