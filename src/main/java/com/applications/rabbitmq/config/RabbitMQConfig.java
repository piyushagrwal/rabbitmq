package com.applications.rabbitmq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

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
}
