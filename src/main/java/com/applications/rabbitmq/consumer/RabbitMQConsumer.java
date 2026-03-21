package com.applications.rabbitmq.consumer;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class RabbitMQConsumer {

    // Min 3 consumers and max 7 consumers for multiple consumers concurrency
    @RabbitListener(queues = "queue_name", concurrency = "3-7")
    public void listen(String message){

    }

    // for multiple queues
    @RabbitListener(queues = {"queue1", "queue2","queue3"})
    public void listenMultiple(String message){
    }

    @RabbitListener(queues = "queue")
    public void listenException(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        int n = 1000;
        channel.basicReject(tag, true);
        channel.basicAck(tag, false);
        if(n > 500){
            // for consumer to not queue the message again in case of exception
            // Solution - 1
            throw new AmqpRejectAndDontRequeueException("exception text request not queued");
        }

    }
}
