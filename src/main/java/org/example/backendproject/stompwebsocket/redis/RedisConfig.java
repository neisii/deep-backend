package org.example.backendproject.stompwebsocket.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final RedisSubscriber redisSubscriber;
    private final LettuceConnectionFactory redisConnectionFactory;

    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);

        container.addMessageListener(new MessageListenerAdapter(redisSubscriber), new PatternTopic("room.*"));

        return container;
    }
}
