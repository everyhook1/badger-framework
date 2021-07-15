package org.badger.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liubin01
 */
@RestController
@SpringBootApplication
public class ConsumerApplication {

    @Autowired
    private UserInfo userInfo;

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }

    @GetMapping
    public String echo(String name) {
        return userInfo.echo(name);
    }
}
