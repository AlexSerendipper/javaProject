package community.mapper;

import community.pojo.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 @author Alex
 @create 2023-04-09-19:17
 */
@Mapper
public interface MessageMapper {
    /**
     * 查询结果为查出【当前用户】的的所有会话的最新一条私信。并降序排序
     * @param userId
     * @return
     */
    List<Message> selectConversations(int userId);

    // 查询当前用户的会话数量
    int selectConversationCount(int userId);

    /**
     * 查询结果为某个会话所包含的所有私信
     * @param conversationId
     * @return
     */
    List<Message> selectLetters(String conversationId);


    /**
     * 查询结果为某个会话中所包含的私信的数量
     * @param conversationId
     * @return
     */
    int selectLetterCount(String conversationId);

    // 查询未读私信的数量(这里实现如果传入conversationId就查询某一个会话的未读消息，如果不传入conversationId，就查询所有会话的未读消息数量)
    int selectLetterUnreadCount(int userId, String conversationId);

    // 新增消息
    int insertMessage(Message message);

    /**
     * 修改消息的状态，为了可以一次修改多个消息的状态，可以传入多个ID
     * @param ids
     * @param status
     * @return
     */
    int updateStatus(List<Integer> ids, int status);

    // 查询某个主题(评论、点赞、关注)下最新的通知
    Message selectLatestNotice(int userId, String topic);

    /**
     * 查询某个主题所包含的通知数量，当不传入topic时，则查询所有主题的未读消息的数量
     * @param userId
     * @param topic
     * @return
     */
    int selectNoticeCount(int userId, String topic);

    // 查询未读的通知的数量
    int selectNoticeUnreadCount(int userId, String topic);

    // 查询某个主题所包含的所有通知
    List<Message> selectNotices(int userId, String topic);

}
