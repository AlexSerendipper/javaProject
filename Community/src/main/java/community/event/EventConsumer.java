package community.event;

import com.alibaba.fastjson.JSONObject;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import community.pojo.DiscussPost;
import community.pojo.Event;
import community.pojo.Message;
import community.service.DiscussPostService;
import community.service.ElasticSearchService;
import community.service.MessageService;
import community.util.CommunityConstant;
import community.util.CommunityUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 @author Alex
 @create 2023-04-14-14:24
 */
@Component
public class EventConsumer implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);
    @Autowired
    private MessageService messageService;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private ElasticSearchService elasticSearchService;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${wk.image.command}")
    private String wkImageCommand;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;

    // 这里使用普通的定时任务线程池即可，因为虽然在多台服务器上都有eventConsumer，但是consumer之间是有一个抢占关系的，谁抢到谁执行，并不存在分布式的问题
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;


    // 消费评论、点赞、关注事件（发送系统通知）
    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_FOLLOW,TOPIC_LIKE})
    public void receiveMessage(ConsumerRecord record){
        if(record==null || record.value()==null){
            logger.error("消息的内容为空");
            return;
        }
        // 若消息不为空，接收到的消息为json格式的字符串，我们将其转换回event对象
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event==null){
            logger.error("消息格式错误！");
        }

        // 消费者得到一个event对象，需要将消息进行转换，并存储到message表中，系统消息的 from_id为1，conversationId为当前topic
        // 注意此时存入表中的content是 为了能正确显示系统通知 所需要的数据组成的json字符串{entityType,entityId,postId,userId,content}(content为map转换成的json字符串)
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());
        HashMap<String, Object> content = new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId",event.getEntityId());
        if(!event.getData().isEmpty()){
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(),entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }

    // 消费发帖事件
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record){
//        System.out.println("我收到消息了！");
        if(record==null || record.value()==null){
            logger.error("消息的内容为空");
            return;
        }
        // 若消息不为空，接收到的消息为json格式的字符串，我们将其转换回event对象
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event==null){
            logger.error("消息格式错误！");
        }

        // 消费者得到一个event对象，需要将消息进行转换，根据事件中的信息，查询到对应的帖子 存储到 elasticSearch 服务器中
        DiscussPost post = discussPostService.getDiscussPostById(event.getEntityId());
        elasticSearchService.saveDiscusspost(post);
    }


    // 消费删帖事件
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record){
//        System.out.println("我收到消息了！");
        if(record==null || record.value()==null){
            logger.error("消息的内容为空");
            return;
        }
        // 若消息不为空，接收到的消息为json格式的字符串，我们将其转换回event对象
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event==null){
            logger.error("消息格式错误！");
        }

        elasticSearchService.deleteDiscusspost(event.getEntityId());
    }


    // 消费分享事件
    @KafkaListener(topics = {TOPIC_SHARE})
    public void handleShareMessage(ConsumerRecord record){
//        System.out.println("我收到消息了！");
        if(record==null || record.value()==null){
            logger.error("消息的内容为空");
            return;
        }
        // 若消息不为空，接收到的消息为json格式的字符串，我们将其转换回event对象
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event==null){
            logger.error("消息格式错误！");
        }

        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");

        // 生成wk命令
        String cmd = wkImageCommand + " " + htmlUrl + " " + wkImageStorage + "/" + fileName + suffix;
        try {
            Runtime.getRuntime().exec(cmd);  // 在java中，这条命令只是将命令异步交给cmd去执行，直接就执行下一步了
            logger.info("生成长图成功：" + cmd);
        } catch (IOException e) {
            logger.info("生成长图失败：" + e.getMessage());

        }

        // 将图片上传到七牛云上
        // 启用定时器，监视该图片是否生成完成，一旦生成了，则上传到七牛云
//        uploadTask task = new uploadTask(fileName, suffix);
//        ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(task, 500);  // 每500ms检查一下，看一下图片是否生成
//        task.setFuture(future);
    }


    class uploadTask implements Runnable{
        // 文件名称
        private String fileName;
        // 文件后缀
        private String suffix;
        // 启动任务的返回值，用来停止定时器
        private ScheduledFuture future;
        // 开始时间
        private long startTime;
        // 上传次数
        private Integer uploadCount;

        public uploadTask(String fileName, String suffix) {
            this.fileName = fileName;
            this.suffix = suffix;
            this.startTime= System.currentTimeMillis();
            this.uploadCount=0;
        }

        public void setFuture(ScheduledFuture future) {
            this.future = future;
        }

        @Override
        public void run() {
            // 是有一些情况 文件上传失败，所以要保证，无论发生了什么，都要关闭定时任务
            // 生成失败1
            if(System.currentTimeMillis() - startTime > 30000){
                logger.error("执行时间过长，终止任务" + fileName);
                future.cancel(true);
                return;
            }
            // 生成失败2
            if(uploadCount>=3){
                logger.error("上传次数过多，终止任务" + fileName);
                future.cancel(true);
                return;
            }
            // 上传功能，这里上传 是一直被重复调用的
            String path = wkImageStorage + "/" + fileName + suffix;
            File file = new File(path);
            if(file.exists()){
                logger.info(String.format("开始第%d次上传[%s].", ++uploadCount, fileName));
                // 设置响应信息（七牛云特定的stringMap格式）
                StringMap policy = new StringMap();
                policy.put("returnBody", CommunityUtil.getJsonString(0));
                // 生成上传凭证
                Auth auth = Auth.create(accessKey,secretKey);
                String uploadToken = auth.uploadToken(shareBucketName,fileName,3600,policy);
                // 指定上传的机房(就是上传到的华南区的地址) (上传头像功能中是直接在异步请求中指定了上传的地址)
                UploadManager manager = new UploadManager(new Configuration(Zone.huanan()));

                try {
                    // 开始上传图片（格式基本固定）
                    Response res = manager.put(path, fileName, uploadToken, null, "image/" + suffix, false);
                    // 处理响应的结果(从返回的json格式字符串，转换为json格式的对象)
                    JSONObject json = JSONObject.parseObject(res.bodyString());
                    if(json == null || json.get("code")==null || !json.get("code").toString().equals("0")){
                        logger.info(String.format("第%d次上传失败[%s]", uploadCount,fileName));
                    }else {
                        logger.info(String.format("第%d次上传成功[%s]", uploadCount,fileName));
                        // 上传成功，关闭定时任务
                        future.cancel(true);
                    }
                } catch (QiniuException e) {
                    logger.info(String.format("第%d次上传失败[%s]", uploadCount,fileName));
                }
            }else {
                logger.info("图片未生成，等待图片生成[" + fileName + "]");
            }
        }
        //
    }
}
