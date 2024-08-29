package com.xiaobai1226.aether.core.service.impl;

import com.xiaobai1226.aether.core.service.intf.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

/**
 * 邮箱服务实现类
 *
 * @author bai
 */
@Component
@Slf4j
public class EmailServiceImpl implements EmailService {

    // TODO 直接用的网上的代码，待自己完善

    //Spring Boot 提供了一个发送邮件的简单抽象，使用的是下面这个接口，这里直接注入即可使用
//    @Autowired
//    private JavaMailSender mailSender;

    // 配置文件中我的qq邮箱
    @Inject("${spring.mail.username:}")
    private String from;

    @Override
    public Boolean sendSimpleMail(String to, String subject, String content) {
        try {
            //创建SimpleMailMessage对象
//            SimpleMailMessage message = new SimpleMailMessage();
//            //邮件发送人
//            message.setFrom(from);
//            //邮件接收人
//            message.setTo(to);
//            //邮件主题
//            message.setSubject(subject);
//            //邮件内容
//            message.setText(content);
//            //发送邮件
//            mailSender.send(message);

            return true;
        } catch (Exception e) {
            log.error("发送邮件时发生异常！", e);
            return false;
        }
    }

    @Override
    public Boolean sendHtmlMail(String to, String subject, String content) {
        //获取MimeMessage对象
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper messageHelper;
        try {
//            messageHelper = new MimeMessageHelper(message, true);
//            //邮件发送人
//            messageHelper.setFrom(from);
//            //邮件接收人,设置多个收件人地址
//            InternetAddress[] internetAddressTo = InternetAddress.parse(to);
//            messageHelper.setTo(internetAddressTo);
//            //messageHelper.setTo(to);
//            //邮件主题
//            message.setSubject(subject);
//            //邮件内容，html格式
//            messageHelper.setText(content, true);
//            //发送
//            mailSender.send(message);
//            //日志信息
//            log.info("邮件已经发送。");

            return true;
        } catch (Exception e) {
            log.error("发送邮件时发生异常！", e);
            return false;
        }
    }

    @Override
    public Boolean sendAttachmentsMail(String to, String subject, String content, String filePath) {
//        MimeMessage message = mailSender.createMimeMessage();
        try {
//            MimeMessageHelper helper = new MimeMessageHelper(message, true);
//            helper.setFrom(from);
//            helper.setTo(to);
//            helper.setSubject(subject);
//            helper.setText(content, true);
//
//            FileSystemResource file = new FileSystemResource(new File(filePath));
//            String fileName = filePath.substring(filePath.lastIndexOf(File.separator));
//            helper.addAttachment(fileName, file);
//            mailSender.send(message);
            //日志信息
            log.info("邮件已经发送。");

            return true;
        } catch (Exception e) {
            log.error("发送邮件时发生异常！", e);
            return false;
        }
    }
}
