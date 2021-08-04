package cn.mengfly.springui;

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
    @Value("${server.port}")
    public int port;

    public static void main(String[] args) {
        SpringApplication.run(SpringUiApplication.class, args);
    }


    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
//        int i = 1/0;
        System.out.println("test serverPort :" + port);

    }
}
