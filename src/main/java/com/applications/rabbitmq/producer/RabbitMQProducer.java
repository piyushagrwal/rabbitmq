package com.applications.rabbitmq.producer;

import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.Correlation;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.rabbit.stream.producer.RabbitStreamTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RabbitMQProducer {

    // Use this service class from other service to publish message
    // then the listener can read message and do it action
    // create queue in rabbit mq env
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RabbitStreamTemplate rabbitStreamTemplate;

//    @Scheduled(fixedRate = 500) to send message after every fixed time interval
//    @Scheduled(cron = "0 0 23 * * *") spring cron
//    spring cron -> second  minute  hour  day-of-month  month  day-of-week
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


    // for getting status if the message to queue is published
    @PostConstruct
    private void registerCallBack(){
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if(correlationData == null){
                return;
            }
            if(ack){
                // message is published correlationData.getId()
            }else{
                // Invalid exchange message not published
            }
        });

        rabbitTemplate.setReturnsCallback(returned -> {
            if(returned.getReplyText() != null && returned.getReplyText().equalsIgnoreCase("NO ROUTE")){
                var id = returned.getMessage().getMessageProperties().getHeader("spring_returned_message_correlation");
                // No routing key for the id
            }
        });
    }

    // Send message with correlation data
    public void sendMessageWithCorrelation(String message){
        var correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend("","",message,correlationData);
    }

    // for superstream producer
    public void sendMessageSuperstream(int start, int end){
        var partitions = 3;
        for(int i = start; i<= end; i++) {
            var str = Integer.toString(i);
            var message = rabbitStreamTemplate.messageBuilder().addData(str.getBytes())
                    .properties().messageId(i).messageBuilder().build();
            rabbitStreamTemplate.send(message);
        }

    }

}
