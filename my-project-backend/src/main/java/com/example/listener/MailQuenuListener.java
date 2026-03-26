package com.example.listener;


import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * 获取消息队列的数据
 */
@Component  //注册为Bean
@RabbitListener(queues = "mail") //定义此方法为队列mail的监听器，一旦监听到新的消息，就会接受并处理
public class MailQuenuListener {
    @Autowired
    JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    String username;
    @RabbitHandler//当监听到新的消息时，会自动调用该方法来处理消息
    /**
     * 发送验证码
     */
    public void sendMailMessage(Map<String, Objects> data){

        Object emailObject = data.get("email");
        String email = (emailObject != null) ? emailObject.toString() : null;
        Integer code = Integer.parseInt(String.valueOf(data.get("code")));
        //前端选择是register还是reset
        Object typeObject = data.get("type");
        String type = (typeObject != null) ? typeObject.toString() : null;
        //判断进行什么操作时发送的验证码
        SimpleMailMessage message=switch (type){
            case "register" ->
                createMessage("欢迎注册我们的网站",
                        "您的本次邮箱注册验证码为："+code+",有效时间为三分钟,为了保证您的安全，请勿泄露验证码信息",email);
            case "reset" ->
                    createMessage("您好，您正在进行重置密码操作","本次邮箱验证码:"+code+"，有效时间为三分钟,如非本人操作，请忽视",email);
            case "modify" ->
                    createMessage("您好，您正在进行修改电子邮件操作","本次邮箱验证码:"+code+"，有效时间为三分钟,如非本人操作，请忽视",email);
            default -> null;
        };
        if(message==null) return;
        //发送
        mailSender.send(message);
    }

    /**
     * 创建邮箱发送类
     * @param title
     * @param content
     * @param email
     * @return
     */
    private SimpleMailMessage createMessage(String title,String content,String email){
        SimpleMailMessage simpleMailMessage=new SimpleMailMessage();
        simpleMailMessage.setSubject(title);
        simpleMailMessage.setText(content);
        simpleMailMessage.setTo(email);
        simpleMailMessage.setFrom(username);
        return simpleMailMessage;
    }

}