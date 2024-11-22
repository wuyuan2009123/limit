package com.hsdb.limit.controller;

import com.hsdb.limit.aspect.RedissonRateLimit;
import com.hsdb.limit.req.PostParamReq;
import com.hsdb.limit.req.PutParamReq;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DemoController {

    @GetMapping("/api1")
    @RedissonRateLimit(key = "'getKey:'+#userName", timeOut = 1, count = 100)
    public String get(String userName) {
        return "get method" + userName;
    }

    @PostMapping("/api2")
    @RedissonRateLimit(key = "'postKey:'+#postParamReq.userName", timeOut = 1, count = 100)
    public String post(@RequestBody PostParamReq postParamReq) {
        return "post method" + postParamReq.getUserName();
    }

    @PutMapping("/api3")
    @RedissonRateLimit(key = "'putKey:'+#putParamReq.userName", timeOut = 1, count = 100)
    public String put(@RequestBody PutParamReq putParamReq) {
        return "put method" + putParamReq.getUserName();
    }


}
