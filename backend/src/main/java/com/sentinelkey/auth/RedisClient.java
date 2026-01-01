package com.sentinelkey.auth;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@ApplicationScoped
public class RedisClient {

    private JedisPool jedisPool;

    @PostConstruct
    public void init() {
        // Simulated Cluster connection (Standalone for dev, but interface ready for cluster)
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        this.jedisPool = new JedisPool(poolConfig, "localhost", 6379);
    }

    public boolean exists(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        }
    }

    public void setEx(String key, int seconds, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(key, seconds, value);
        }
    }

    public String get(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }

    @PreDestroy
    public void close() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}
