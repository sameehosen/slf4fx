package org.room13.slf4fx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Bootstrap class for SLF4Fx server.
 */
public class SLF4FxServer {
    private static final Logger _log = LoggerFactory.getLogger(SLF4FxServer.class);

    public static void main(String[] args) {
        final long startTime = System.currentTimeMillis();
        final MessageSource resources = new ClassPathXmlApplicationContext("/META-INF/slf4fx-context.xml");
        final String version = resources.getMessage("slf4fx.version", null, null);
        final String buildNumber = resources.getMessage("slf4fx.buildNumber", null, null);
        _log.info("slf4fx ({}.{}) server started in {}ms", new Object[]{
                version, buildNumber, System.currentTimeMillis() - startTime});
    }
}
