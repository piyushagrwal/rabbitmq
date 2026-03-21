package com.applications.rabbitmq.entity;

public class RabbitMqQueue {
    private String message;
    private String name;

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RabbitMqQueue(String message, String name) {
        this.message = message;
        this.name = name;
    }
}
