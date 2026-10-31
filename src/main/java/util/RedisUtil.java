package util;

import lombok.Getter;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {
    //포트번호 임시로 설정함
    @Getter
    private static final JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);

}
