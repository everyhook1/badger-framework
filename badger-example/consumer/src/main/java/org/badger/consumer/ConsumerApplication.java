package org.badger.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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

    @GetMapping(value = "echo")
    public String echo(@RequestParam("str") String str) {
        return userInfo.echo(str);
    }

    @GetMapping(value = "sum")
    public int sum(@RequestParam("a") int a,
                   @RequestParam("b") int b) {
        return userInfo.sum(a, b);
    }

    @GetMapping(value = "getStrings")
    public List<String> getStrings(@RequestParam("str") String str) {
        return userInfo.getStrings(str);
    }

    @GetMapping(value = "getMap")
    public Map<Integer, String> getMap(@RequestParam("ids") List<Integer> ids) {
        return userInfo.getMap(ids);
    }
}
