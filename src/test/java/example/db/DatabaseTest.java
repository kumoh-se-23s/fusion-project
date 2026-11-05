package example.db;

import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;


public abstract class DatabaseTest {
    protected InitDB db;

    @BeforeEach
    protected void setUpDatabase() {
        db = InitDB.load();
    }

    //DB에서 읽으면 소수 자릿수가 달라지므로(10000 -> 10000.00) 값만 비교한다
    protected static void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), "expected " + expected + " but was " + actual);
    }
}
