/*
 * Copyright (C) 2018 Conductor Tecnologia SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.conductor.heimdall.api.configuration;

import br.com.conductor.heimdall.core.entity.RateLimit;
import br.com.conductor.heimdall.core.publisher.MessagePublisher;
import br.com.conductor.heimdall.core.publisher.RedisMessagePublisher;
import br.com.conductor.heimdall.core.util.ConstantsCache;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Class responsible for the Redis configuration.
 *
 * @author Marcos Filho
 * @author Marcelo Aguiar Rodrigues
 * @see <a href="https://redis.io/">https://redis.io/</a>
 */
@Configuration
@Profile("!test")
public class RedisConfiguration {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    /**
     * Creates a new {@link JedisConnectionFactory}.
     *
     * @return {@link JedisConnectionFactory}
     */
    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {

        RedisStandaloneConfiguration redisStandaloneConfiguration
                = new RedisStandaloneConfiguration(redisHost, redisPort);

        return new JedisConnectionFactory(redisStandaloneConfiguration);

    }

    /**
     * Returns a configured {@link RedisTemplate}.
     *
     * @return {@link RedisTemplate} Object, Object
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new JdkSerializationRedisSerializer());
        redisTemplate.setEnableTransactionSupport(true);

        return redisTemplate;
    }

    @Bean
    MessagePublisher redisPublisher() {
        return new RedisMessagePublisher(redisTemplate());
    }

    /**
     * Returns a configured {@link RedisTemplate}.
     *
     * @return {@link RedisTemplate} String, {@link RateLimit}
     */
    @Bean
    public RedisTemplate<String, RateLimit> redisTemplateRate() {

        RedisTemplate<String, RateLimit> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setEnableTransactionSupport(true);

        return redisTemplate;
    }

    /**
     * Returns a configured {@link RedissonClient}.
     *
     * @return {@link RedissonClient}
     */
    @Bean
    public RedissonClient redissonClientRateLimitInterceptor() {

        return createConnection(ConstantsCache.RATE_LIMIT_DATABASE);
    }

    @Bean
    public RedissonClient redissonClientCacheInterceptor() {

        return createConnection(ConstantsCache.CACHE_INTERCEPTOR_DATABASE);
    }

    private RedissonClient createConnection(int database) {

        Config config = new Config();
        config.useSingleServer()
                .setAddress(redisHost + ":" + redisPort)
                .setDatabase(database);

        return Redisson.create(config);
    }
}