package community.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;

/**
 @author Alex
 @create 2023-04-04-21:46
 */

@Component
public class MailClient {
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);
    @Autowired
    private JavaMailSender mailSender;

    // 发件人账号，直接关联配置文件
    @Value("${spring.mail.username}")
    private String sender;

    public void sendMail(String receiver,String subject,String content) throws Exception {
        // 1、创建mimemessage对象，使用它来实现发送邮件功能
        MimeMessage message = mailSender.createMimeMessage();
        // 2、使用MimeMessageHelper来帮助实现发送邮件功能
        MimeMessageHelper helper = new MimeMessageHelper(message);
        // 3. 设置发件人和收件人
        helper.setFrom(sender);
        helper.setTo(receiver);
        // 4. 设置邮件的主题 和 内容(第二个参数表示支持使用html文本，即支持传入各种标签)
        helper.setSubject(subject);
        helper.setText(content,true);
        // 5. 发送邮件
        mailSender.send(helper.getMimeMessage());
    }
}
