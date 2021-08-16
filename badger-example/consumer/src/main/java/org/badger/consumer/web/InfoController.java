package org.badger.consumer.web;

import org.badger.common.api.RpcProxy;
import org.badger.consumer.tcc.AccountService;
import org.badger.example.api.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author liubin01
 */
@RestController
public class InfoController {

    @Autowired
    @RpcProxy(serviceName = "badger-example")
    private UserInfo userInfo;

    @Autowired
    private AccountService accountService;

    @GetMapping(value = "try")
    public void tryM(@RequestParam("a") int a,
                     @RequestParam("b") int b) {
        accountService.tryM(a, b);
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
