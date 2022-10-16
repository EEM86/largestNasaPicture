package com.bobocode.largestnasapic.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

  @Bean
  public Queue picturesQueue() {
    return QueueBuilder.durable("largest-picture-command-queue")
        .withArgument("x-dead-letter-exchange", "")
        .withArgument("x-dead-letter-routing-key", "nasa-pictures-dlq")
        .build();
  }

  @Bean
  public Queue picturesDlq() {
    return new Queue("nasa-pictures-dlq");
  }

  @Bean
  public Exchange pictureExchange() {
    return new DirectExchange("nasa-pictures-exchange");
  }

  @Bean
  public Binding picturesQueueBinding() {
    return BindingBuilder
        .bind(picturesQueue())
        .to(pictureExchange())
        .with("")
        .noargs();
  }

  @Bean
  public MessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
  }

}
