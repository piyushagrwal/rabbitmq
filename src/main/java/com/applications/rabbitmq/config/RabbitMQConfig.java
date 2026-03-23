package com.applications.rabbitmq.config;

import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.OffsetSpecification;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.amqp.autoconfigure.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.rabbit.stream.config.SuperStream;
import org.springframework.rabbit.stream.listener.StreamListenerContainer;
import org.springframework.rabbit.stream.producer.RabbitStreamTemplate;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Configuration
public class RabbitMQConfig {

    @Bean
    ObjectMapper objectMapper(){
        return JsonMapper.builder().findAndAddModules().build();
    }
    // Then to convert, use mapper.writeValueAsString(object);
    // after autowiring ObjectMapper
    // to get the json of an object as string and send it
    // to read object, objectMapper.readValue(message, Employee.class)

    @Bean
    RabbitListenerContainerFactory<SimpleMessageListenerContainer> prefetchOneContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory
    ){
        var factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setPrefetchCount(1);
        // for adding offset
        var tOffset = ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(5).toEpochSecond() * 1000;
//        factory.setNativeListener(true);
//        factory.setConsumerCustomizer((id, builder) -> {
//            builder.name("consumer_name").offset(OffsetSpecification.offset(3)).autoTrackingStrategy();
//            builder.name("consumer_name").offset(OffsetSpecification.next()).autoTrackingStrategy();
//            builder.name("consumer_name").offset(OffsetSpecification.timestamp(tOffset)).autoTrackingStrategy();
//            builder.name("consumer_name").offset(OffsetSpecification.first()).autoTrackingStrategy();
//            builder.name("consumer_name").offset(OffsetSpecification.offset(3)).manualTrackingStrategy();
//            for single active consumer with offset tracking
//            builder.name("consumer_name").offset(OffsetSpecification.offset(3)).singleActiveConsumer().autoTrackingStrategy();
//        })
        return factory;
    }

    // To create an exchange
//    @Bean
//    FanoutExchange fanoutExchange(){
//        return new FanoutExchange("exchange-name",true, false, null);
//    }
//
//    // To create queue
//    @Bean
//    Queue queue(){
//        return new Queue("queue-name");
//    }
//
//    @Bean
//    Binding binding(){
//        return new Binding("queue-name", Binding.DestinationType.QUEUE, "exchange-name", "", null);
//        return BindingBuilder.bind(queue()).to(new DirectExchange("exchange")).with("routing-key");
//    }

    // To create schema in one method
    @Bean
    Declarables createRabbitmqSchema(){
        return new Declarables(new FanoutExchange("exchange-name",true, false, null),
                new Queue("queue-name"),
                new Binding("queue-name", Binding.DestinationType.QUEUE, "exchange-name", "", null)
                );
    }

    // for creating rabbitmq stream, then use it to send message in producer
    @Bean
    @Qualifier("rabbitmqstreamtemplate")
    RabbitStreamTemplate rabbitStreamTemplate(Environment env){
        var template = new RabbitStreamTemplate(env, "stream-name");
//        for super stream
//        template.setSuperStreamRouting(message -> {
//            return message.getProperties().getMessageIdAsString();
//        });
        return template;
    }

    @Bean
    SuperStream superStream(){
        // name and partitions
        return new SuperStream("stream_name", 3);
    }

    // For super stream listener and single active consumer
    @Bean
    StreamListenerContainer streamListenerContainer(){
        var env = Environment.builder().maxConsumersByConnection(1).build();
        var container = new StreamListenerContainer(env);
        container.setConsumerCustomizer((id, builder) -> builder.offset(OffsetSpecification.first()));
        container.superStream("stream_name","consumer_stream_name");
        container.setupMessageListener(msg -> {
            // log new String(msg.getBody())
        });
        return container;
    }

    // for sending json objects in message stream
    // Then use convertandsend in producer
    // User obejctmapper in consumer to read json from (Data) message.getBody().getValue().getArray()
    @Bean
    MessageConverter converter(@Autowired ObjectMapper objectMapper){
        return new JacksonJsonMessageConverter();
    }

    RabbitStreamTemplate streamjsontemplate(Environment env, JacksonJsonMessageConverter jsonMessageConverter){
        var template = new RabbitStreamTemplate(env, "stream_name");
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
    
}
