package community.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import community.mapper.CommentMapper;
import community.pojo.Comment;
import community.pojo.DiscussPost;
import community.util.CommunityConstant;
import community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 @author Alex
 @create 2023-04-08-21:43
 */
@Service
public class CommentService implements CommunityConstant {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;
    /**
     * 返回评论的分页数据
     * @param entityType
     * @param entityId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageInfo<Comment> getCommentByEntity(int entityType,int entityId,int pageNum,int pageSize){
        PageHelper.startPage(pageNum, pageSize);
        List<Comment> comments = commentMapper.selectCommentsByEntity(entityType, entityId);
        // 设置导航栏显示5
        PageInfo<Comment> pageInfo = new PageInfo<>(comments, 5);
        return pageInfo;
    }

    /**
     * 返回评论总数
     * @param entityType
     * @param entityId
     * @return
     */
    public int findCommentCount(int entityType,int entityId){
        return commentMapper.countCommentsByEntity(entityType,entityId);
    }

    /**
     * 添加评论/回复/讨论
     * @param comment
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        if(comment==null){
            throw new IllegalArgumentException("参数不能为空");
        }
        // 评论过滤
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        // 添加评论
        int i = commentMapper.insertComment(comment);
        // 更新帖子的评论数量
        if(comment.getEntityType()==ENTITY_TYPE_POST){
            int count = commentMapper.countCommentsByEntity(comment.getEntityType(), comment.getEntityId());
            // 你添加完就是count数量就是对的了
            discussPostService.updateCommentCount(comment.getEntityId(),count);
        }
        return i;
    }

    /**
     * 根据id查询comment
     * @param id
     * @return
     */
    public Comment getCommentById(int id){
        return commentMapper.selectCommentsById(id);
    }
}
