package com.applications.rabbitmq.consumer;

import com.applications.rabbitmq.entity.Employee;
import com.rabbitmq.client.Channel;
import com.rabbitmq.stream.MessageHandler;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

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

    // for using our prefetch count for the method by default spring if not defined
    @RabbitListener(queues = "queue_name", concurrency = "3-7", containerFactory = "prefetchOneContainerFactory")
    public void listenUsingFactory(String message){

    }

    // To listen to multiple types of objects based on headers
    @RabbitHandler
    public void listenMultipleTypes(Employee employee){

    }

    // for default listen multiple types of objects based on headers
    @RabbitHandler(isDefault = true)
    public void listenMultipleTypesdefault(Object employee){

    }

    // Based on consumer output send message to queue

    @RabbitHandler
    @SendTo("exchange_name/routing_key")
    public String listenandSendReponsetoother(Object employee){
        boolean random = ThreadLocalRandom.current().nextBoolean();
        if(random){
            // send response 1
        }else{
            // send response 2
        }
        return "";
    }

    // To create queue and exchange by code
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "queue_name", durable = "true"),
            exchange = @Exchange(name = "exchange_name", type = ExchangeTypes.DIRECT, durable = "true"),
            key = "routing_key",
            ignoreDeclarationExceptions = "true"))
    public void createQueueAndExchange(String message){

    }

    // For offset tracking
    @RabbitListener(queues = "queue_name", containerFactory = "container_factory")
    public void listenOffset(Message message, MessageHandler.Context context){
        // log.info("", message.getBody(), context.offset());
        // for manual tracking strategy only
        context.storeOffset();
    }
}
