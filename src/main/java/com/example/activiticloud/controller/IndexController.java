package com.example.activiticloud.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class IndexController {
    @ResponseBody
    @RequestMapping("/")
    public String getTestString(){
        return "hello world";
    }
}
