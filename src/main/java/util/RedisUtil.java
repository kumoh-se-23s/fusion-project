package util;

import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Optional;

public class RedisUtil {
    @Getter
    private static final JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);

    public void save(String key, Object value, int duration) {
        try (Jedis jedis = getPool().getResource()) {
            jedis.setex(key, duration, value.toString());
        }
    }

    public Optional<String> getValue(String key) {
        try(Jedis jedis = getPool().getResource()) {
            String value = jedis.get(key);

            if(value == null) {
                return Optional.empty();
            }

            return Optional.of(value);
        }
    }

    public boolean checkWhiteList(String userId , String token) {
        try (Jedis jedis = getPool().getResource()) {
            String value = jedis.get(userId);

            if(value == null) {
                return false;
            } else {
                return value.equals(token);
            }
        }
    }
}
