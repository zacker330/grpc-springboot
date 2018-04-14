package codes.showme.hello.controller;

import codes.showme.hello.service.HelloService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class HelloController {


    @Resource
    private HelloService helloService;

    @RequestMapping("/hello/{word}")
    public String index(@PathVariable String word) {
        return helloService.hello(word);
    }

}
