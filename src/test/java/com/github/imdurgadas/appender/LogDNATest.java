package com.github.imdurgadas.appender;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.slf4j.MDC;

/**
 * @author imDurgadas
 */
@Slf4j
public class LogDNATest {

    @Test
    public void testLogDNALogbackConf() throws InterruptedException {
        MDC.put("customerId", "001");
        MDC.put("requestId", "x732432");

        log.debug("Just fyi");
        log.info("You should know !!");
        log.warn("Need to be careful even though developers don't care warnings");
        log.error("Should never had happen");
        try {
            throw new RuntimeException();
        } catch (Exception e) {
            log.error("Oops, something bad , need to know the root ", e);
        }
        Thread.sleep(20000);
    }
}
