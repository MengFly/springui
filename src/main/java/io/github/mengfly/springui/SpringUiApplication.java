package io.github.mengfly.springui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

/**
 * @author Mengfly
 * @date 2021/7/29 17:57
 */
@SpringBootApplication
public class SpringUiApplication implements ApplicationListener<ApplicationStartedEvent> {
    static Log log = LogFactory.getLog(SpringUiApplication.class);
    @Value("${server.port}")
    public int port;

    public static void main(String[] args) {
        SpringApplication.run(SpringUiApplication.class, args);
    }


    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
//        int i = 1/0;
//        new Thread(() -> {
//
//            while (true) {
//                log.info("test serverPort :" + port);
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

    }
}
