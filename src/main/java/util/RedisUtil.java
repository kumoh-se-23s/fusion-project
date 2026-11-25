package util;

import config.AuthProperties;
import constant.RedisKey;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Objects;
import java.util.Optional;

/**
 * Redis에 토큰 저장 형식 <br>
 * 키 : access or refresh + userId + " : " <br>
 * 밸류 : jti <br>
 * access + userId + " : "  , jti <br>
 * refresh + userId + " : " , jti
 */


public class RedisUtil {
    @Getter
    private static final JedisPool pool = new JedisPool(new JedisPoolConfig(), AuthProperties.get("redis.host"), AuthProperties.getInt("redis.port", 0), AuthProperties.getInt("redis.timeout", 0), AuthProperties.get("redis.password"));

    public void save(RedisKey redisKey, Object key, Object value, int duration) {
        try (Jedis jedis = getPool().getResource()) {
            jedis.setex(redisKey.getFormat() + key + " : ", duration, value.toString());
        }
    }

    public Optional<String> getValue(RedisKey redisKey, Object key) {
        try (Jedis jedis = getPool().getResource()) {
            String value = jedis.get(redisKey.getFormat() + key + " : ");

            if (value == null) {
                return Optional.empty();
            }

            return Optional.of(value);
        }
    }

    public void delete(RedisKey redisKey, Object userId) {
        try (Jedis jedis = getPool().getResource()) {
            jedis.del(redisKey.getFormat() + userId + " : ");
        }
    }

    public boolean isRegisteredInRedis(String tokenType, String jti, long userId) {
        try (Jedis jedis = getPool().getResource()) {
            String value;

            if (Objects.equals(tokenType, "access")) {
                value = jedis.get("access" + userId + " : ");
            } else {
                value = jedis.get("refresh" + userId + " : ");
            }

            return Objects.equals(value, jti);
        }
    }
}
