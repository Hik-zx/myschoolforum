package com.example.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMq相关配置类
 */
@Configuration
public class RabbitConfiguration {



    /**
     * 创建邮件队列
     * @return
     */
    @Bean("EmailQueue")     //定义消息队列
    public Queue queue(){
        return QueueBuilder
                .durable("mail")   //持久化类型
                .build();
    }
    @Bean
    public MessageConverter jsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }
//    @Bean("binding")
//    public Binding binding(@Qualifier("directExchange") Exchange exchange,
//                           @Qualifier("yydsQueue") Queue queue){
//        //将我们刚刚定义的交换机和队列进行绑定
//        return BindingBuilder
//                .bind(queue)   //绑定队列
//                .to(exchange)  //到交换机
//                .with("my-yyds")   //使用自定义的routingKey
//                .noargs();
//    }
//
//    @Bean("jacksonConverter")   //直接创建一个用于JSON转换的Bean
//    public Jackson2JsonMessageConverter converter(){
//        return new Jackson2JsonMessageConverter();
//    }
}