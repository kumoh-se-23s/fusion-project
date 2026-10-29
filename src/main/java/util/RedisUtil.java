package util;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {
    //포트번호 임시로 설정함
    private static final JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);

    public static JedisPool getPool() {
        return pool;
    }
}
