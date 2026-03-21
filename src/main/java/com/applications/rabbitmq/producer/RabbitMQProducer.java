package com.applications.rabbitmq.producer;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQProducer {

    // Use this service class from other service to publish message
    // then the listener can read message and do it action
    // create queue in rabbit mq env
    @Autowired
    private RabbitTemplate rabbitTemplate;

//    @Scheduled(fixedRate = 500) to send message after every fixed time interval
//    Add @EnableScheduling in Application class

    // To publish message to queue
    public void sendMessageQueue(String message){
        // rabbit template converts message to byte array so we don't have to
        rabbitTemplate.convertAndSend("queue_name", message);
    }

    // To publish message to exchange
    public void sendMessageExchange(String message){
        rabbitTemplate.convertAndSend("exchange_name","routing_key", message);
    }

    // To publish message to topic
    public void sendMessageTopic(String message){
        // routing key of type ap.jpg.image to match routing key of exchange binding
        rabbitTemplate.convertAndSend("exchange_name","routing_key", message);
    }

    // To publish message to exchange with headers
    public void sendMessageExchangeHeaders(String message){
        var messageProperties = new MessageProperties();
        messageProperties.setHeader("header_name", "value");
        var msg = new Message("MessageString".getBytes(), messageProperties);
        rabbitTemplate.convertAndSend("exchange_name","routing_key", msg);
    }
}
