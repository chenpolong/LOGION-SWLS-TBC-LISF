package utils;

import org.junit.Test;

import java.util.Arrays;

public class PollingTest {
    @Test
    public void testPolling() {
        Polling<String> polling = new Polling<>(Arrays.asList("a", "b", "c", "d"));

        for (int i = 0; i < 12; i++) {
            String str = polling.next();
            System.out.println(i + ": " + str);
        }
    }
}