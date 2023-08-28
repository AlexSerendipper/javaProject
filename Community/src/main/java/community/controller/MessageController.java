package community.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import community.pojo.Message;
import community.pojo.User;
import community.service.MessageService;
import community.service.UserService;
import community.util.CommunityConstant;
import community.util.CommunityUtil;
import community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 @author Alex
 @create 2023-04-10-11:16
 */
@Controller
public class MessageController implements CommunityConstant {
    @Autowired
    MessageService messageService;
    @Autowired
    HostHolder hostHolder;
    @Autowired
    UserService userService;

    /**
     * 返回私信列表请求
     * @param model
     * @param pageNum
     * @return
     */
    @GetMapping(value = {"/conversation/list/{pageNum}","/conversation/list"})
    public String getList(Model model, @PathVariable(value = "pageNum",required = false) Integer pageNum){
        if(pageNum==null){
            pageNum=1;
        }
        User user = hostHolder.getUser();
        // 每页显示五条
        PageInfo<Message> conversationPageInfo = messageService.findConversations(user.getId(), pageNum, 5);
        List<Message> conversationList = conversationPageInfo.getList();
        model.addAttribute("pageUrl","/conversation/list/");
        model.addAttribute("pageInfo", conversationPageInfo);

        List<Map<String,Object>> conversions = new ArrayList<>();
        if(conversationList!=null){
            for(Message message:conversationList){
                HashMap<String, Object> map = new HashMap<>();
                // 当前会话的最新的私信
                map.put("message",message);
                // 当前会话的私信总数
                map.put("messageCount",messageService.findLetterCount(message.getConversationId()));
                // 当前会话的未读私信数
                map.put("messageUnread",messageService.findLetterUnreadCount(user.getId(),message.getConversationId()));
                // 当前会话的用户头像, 如果当前用户是该最新私信的发送者，显示的头像要是 接收者的头像。如果当前用户是该最新私信的接收者，显示的头像要是 发送者的头像（总之就是不能显示当前用户的头像）。
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("messageTarget", userService.getUserById(targetId));
                conversions.add(map);
            }
        }
        model.addAttribute("conversations",conversions);

        // 查询 当前所有会话 未读消息(私信)总数
        model.addAttribute("conversionUnreadCount",messageService.findLetterUnreadCount(user.getId(),null));

        // 查询 当前所有会话 未读系统通知的总数
        model.addAttribute("noticeUnreadCount",messageService.findNoticeUnreadCount(user.getId(),null));  // 未读通知总数
        return "/site/letter";
    }

    /**
     * 返回私信详情页
     * @param model
     * @param pageNum
     * @param conversationId
     * @return
     */
    @GetMapping(value = {"/conversation/detail/{conversationId}/{pageNum}","/conversation/detail/{conversationId}"})
    public String getConversationDetail(Model model,@PathVariable(value = "pageNum",required = false) Integer pageNum, @PathVariable(value = "conversationId") String conversationId){
//        Integer.valueOf("anc");
        if(pageNum==null){
            pageNum=1;
        }
        User user = hostHolder.getUser();
        // 每页显示五条
        PageInfo<Message> conversationDetailPageInfo = messageService.findLetters(conversationId, pageNum, 5);
        List<Message> conversationDetailList = conversationDetailPageInfo.getList();
        model.addAttribute("pageUrl","/conversation/detail/" + conversationId + "/");
        model.addAttribute("pageInfo", conversationDetailPageInfo);

        List<Map<String,Object>> messages= new ArrayList<>();
        if(conversationDetailList!=null){
            for(Message message:conversationDetailList){
                HashMap<String, Object> map = new HashMap<>();
                // 一条私信
                map.put("message",message);
                // 一条私信对应的发信人(最终显示的也是发信人的头像)
                map.put("user",userService.getUserById(message.getFromId()));
                messages.add(map);
            }
        }
        model.addAttribute("messages",messages);
        // 查找到 当前会话的 对话者
        User fromUser = getFromUserByConversationId(conversationId);
        model.addAttribute("fromUser",fromUser);
        // 设置消息已读
        List<Integer> ids = getUnreadMessageIdList(conversationDetailList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }
        return "/site/letter-detail";
    }

    private List<Integer> getUnreadMessageIdList(List<Message> messageList){
        List<Integer> ids = new ArrayList<>();
        if(messageList!=null){
            for(Message message:messageList){
                // 这里一定要判断，当前用户，需要是消息的接收者，才把消息未读消息取出来改状态
                if(hostHolder.getUser().getId()==message.getToId() && message.getStatus()==0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    private User getFromUserByConversationId(String conversationId){
        String[] s = conversationId.split("_");
        int i0 = Integer.parseInt(s[0]);
        int i1 = Integer.parseInt(s[1]);
        if(hostHolder.getUser().getId()==i0){
            return userService.getUserById(i1);
        }else {
            return userService.getUserById(i0);
        }
    }

    /**
     * 发送私信请求
     * @param targetUserName
     * @param content
     * @return
     */
    @PostMapping("/message/send")
    @ResponseBody
    public String sendMessage(String targetUserName,String content){
//        Integer.valueOf("anc");
        User targetUser = userService.getUserByName(targetUserName);
        if(targetUser==null){
            return CommunityUtil.getJsonString(1,"目标用户不存在！");
        }
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(targetUser.getId());
        // 创建通信二人的conversationId,其中id值较小的放前面
        message.setConversationId(getConversationIdByUser(targetUser,hostHolder.getUser()));
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message);
        return CommunityUtil.getJsonString(0);
    }

    private String getConversationIdByUser(User user1,User user2){
        Integer id1 = user1.getId();
        Integer id2 = user2.getId();
        if(id1<id2){
            return id1 + "_" + id2;
        }else {
            return id2 + "_" + id1;
        }
    }

    /**
     * 通知列表页面
     */
    @GetMapping("/notice/list")
    public String getNoticeList(Model model){
        User user = hostHolder.getUser();
        // 查询评论类通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        HashMap<String, Object> noticeMap = new HashMap<>();
        if(message!=null){
            noticeMap.put("message",message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            HashMap data = JSONObject.parseObject(content, HashMap.class);
            noticeMap.put("user",userService.getUserById((Integer) data.get("userId")));  // 点赞人的userId
            noticeMap.put("entityType",data.get("entityType"));
            noticeMap.put("entityId",data.get("entityId"));
            noticeMap.put("postId",data.get("postId"));
            noticeMap.put("count",messageService.findNoticeCount(user.getId(),TOPIC_COMMENT));
            noticeMap.put("unreadCount",messageService.findNoticeUnreadCount(user.getId(),TOPIC_COMMENT));
        }else {
            noticeMap.put("message",null);
        }
        model.addAttribute("commentNotice",noticeMap);  // 每一个类型的通知以不同的名字存放


        // 查询点赞类的通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        noticeMap = new HashMap<>();  // 覆盖
        if(message!=null){
            noticeMap.put("message",message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            HashMap data = JSONObject.parseObject(content, HashMap.class);
            noticeMap.put("user",userService.getUserById((Integer) data.get("userId")));
            noticeMap.put("entityType",data.get("entityType"));
            noticeMap.put("entityId",data.get("entityId"));
            noticeMap.put("postId",data.get("postId"));
            noticeMap.put("count",messageService.findNoticeCount(user.getId(),TOPIC_LIKE));
            noticeMap.put("unreadCount",messageService.findNoticeUnreadCount(user.getId(),TOPIC_LIKE));
        }else {
            noticeMap.put("message",null);
        }
        model.addAttribute("likeNotice",noticeMap);


        // 查询关注类的通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        noticeMap = new HashMap<>();  // 覆盖
        if(message!=null){
            noticeMap.put("message",message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            HashMap data = JSONObject.parseObject(content, HashMap.class);
            noticeMap.put("user",userService.getUserById((Integer) data.get("userId")));  // 操作的用户
            noticeMap.put("entityType",data.get("entityType"));
            noticeMap.put("entityId",data.get("entityId"));

            noticeMap.put("count",messageService.findNoticeCount(user.getId(),TOPIC_FOLLOW));
            noticeMap.put("unreadCount",messageService.findNoticeUnreadCount(user.getId(),TOPIC_FOLLOW));
        }else {
            noticeMap.put("message",null);
        }
        model.addAttribute("followNotice",noticeMap);

        model.addAttribute("letterUnreadCount",messageService.findLetterUnreadCount(user.getId(),null));  // 未读私信总数
//        System.out.println(messageService.findNoticeUnreadCount(user.getId(),null));
        model.addAttribute("noticeUnreadCount",messageService.findNoticeUnreadCount(user.getId(),null));  // 未读通知总数
        return "/site/notice";  // 通知列表页面
    }


    /**
     * 通知详情页面
     */
    @GetMapping(value = {"/notice/detail/{topic}/{pageNum}","/notice/detail/{topic}"})
    public String getNoticeDetail(@PathVariable("topic") String topic,@PathVariable(value = "pageNum",required = false) Integer pageNum, Model model){
        if(pageNum==null){
            pageNum=1;
        }
        User user = hostHolder.getUser();
        PageInfo<Message> pageInfo = messageService.findNotices(user.getId(), topic, pageNum, 5);
        List<Message> noticeList = pageInfo.getList();
        ArrayList<Map<String, Object>> noticeVoList = new ArrayList<>();
        if(noticeList!=null){
            for(Message notice:noticeList){
                HashMap<String, Object> map = new HashMap<>();
                // 当前通知
                map.put("notice",notice);
                // 内容，是由consumer以json格式存在message表中的content字段的
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                HashMap data = JSONObject.parseObject(content, HashMap.class);
                map.put("user",userService.getUserById((Integer) data.get("userId")));  // 操作的用户
                map.put("entityType",data.get("entityType"));
                map.put("entityId",data.get("entityId"));
                map.put("postId",data.get("postId"));
                // 发送通知的作者(系统用户就是系统用户的名字)
                map.put("fromUser",userService.getUserById(notice.getFromId()));
                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices",noticeVoList);
        model.addAttribute("pageInfo",pageInfo);
        model.addAttribute("pageUrl","/notice/detail/" + topic + "/");
        // 设置已读（当点击到通知详情页面了，就将未读消息全部修改为已读）
        List<Integer> ids = getUnreadMessageIdList(noticeList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }
        return "/site/notice-detail";
    }
}
