package util;

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

    public void delete(String key) {
        try (Jedis jedis = getPool().getResource()) {
            jedis.del(key);
        }
    }

    public boolean checkWhiteList(String tokenType, String jti, long userId) {
        try (Jedis jedis = getPool().getResource()) {
            String value;

            if(Objects.equals(tokenType, "access")) {
                value = jedis.get("access" + userId + " : ");
            } else {
                value = jedis.get("refresh" + userId + " : ");
            }

            return Objects.equals(value, jti);
        }
    }
}
