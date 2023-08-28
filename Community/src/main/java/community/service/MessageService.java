package community.service;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import community.mapper.MessageMapper;
import community.pojo.Comment;
import community.pojo.DiscussPost;
import community.pojo.Message;
import community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import java.util.List;


// 这里统一分为会话conversation，即私信列表    以及   私信message/letter
@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;
    public PageInfo<Message> findConversations(int userId,int pageNum,int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Message> conversations = messageMapper.selectConversations(userId);
        // 设置导航栏显示5
        PageInfo<Message> pageInfo = new PageInfo<>(conversations, 5);
        return pageInfo;
    }

    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }


    public PageInfo<Message> findLetters(String conversationId,int pageNum,int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Message> messages = messageMapper.selectLetters(conversationId);
        // 设置导航栏显示5
        PageInfo<Message> pageInfo = new PageInfo<>(messages, 5);
        return pageInfo;
    }

    public int findLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }


    public int findLetterUnreadCount(int userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }

    public int addMessage(Message message) {
        // 敏感消息过滤
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    /**
     * 改变消息状态为已读
     * @param ids
     * @return
     */
    public int readMessage(List<Integer> ids) {
        return messageMapper.updateStatus(ids, 1);
    }


    public Message findLatestNotice(int userId, String topic) {
        return messageMapper.selectLatestNotice(userId, topic);
    }

    public int findNoticeCount(int userId, String topic) {
        return messageMapper.selectNoticeCount(userId, topic);
    }

    public int findNoticeUnreadCount(int userId, String topic) {
        return messageMapper.selectNoticeUnreadCount(userId, topic);
    }

    public PageInfo<Message> findNotices(int userId, String topic,int pageNum,int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Message> notices = messageMapper.selectNotices(userId, topic);
        // 设置导航栏显示7
        PageInfo<Message> pageInfo = new PageInfo<>(notices,5);
        return pageInfo;
    }
}
