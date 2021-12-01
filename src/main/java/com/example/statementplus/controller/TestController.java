package com.example.statementplus.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName TestController
 * @Description
 * @Author xsir
 * @Date 2021/12/1 20:55
 * @Version V1.0
 */
@RestController
@RequestMapping("test")
public class TestController {

    @GetMapping("/hello")
    public String test(){
        return "this is dev branch";
    }

}
