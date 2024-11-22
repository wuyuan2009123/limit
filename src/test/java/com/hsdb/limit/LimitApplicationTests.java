package com.hsdb.limit;

import com.hsdb.limit.aspect.TestBean;
import com.hsdb.limit.controller.DemoController;
import com.hsdb.limit.req.PostParamReq;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DemoController.class)
@Slf4j
class LimitApplicationTests {

    @Autowired
    MockMvc mockMvc;

    private final Map<String, TestBean> url = new HashMap<>(8);//for random

    {
        url.put("/api/api1?userName=user1", new TestBean(HttpMethod.GET, "user1"));
        url.put("/api/api2", new TestBean(HttpMethod.POST, new PostParamReq("user2")));
        url.put("/api/api3", new TestBean(HttpMethod.POST, new PostParamReq("user3")));
    }

    /**
     * m每秒 100 次随机请求 api1,api3 api3
     */
    @Test
    void test1() throws InterruptedException {
        int batch = 500;
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        CountDownLatch countDownLatch = new CountDownLatch(url.entrySet().size() * batch);
        for (int i = 0; i < batch; i++) {
            //hashmap random
            url.forEach((url, testBean) -> {
                executorService.submit(() -> {
                    if (testBean.getMethod() == HttpMethod.GET) {
                        try {
                            this.mockMvc.perform(get(url))
                                    .andExpect(status().isOk());
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                    } else if (testBean.getMethod() == HttpMethod.POST) {
                        try {
                            this.mockMvc.perform(post(url, testBean.getObject()))
                                    .andExpect(status().isOk());
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                    } else if (testBean.getMethod() == HttpMethod.PUT) {
                        try {
                            this.mockMvc.perform(put(url, testBean.getObject()))
                                    .andExpect(status().isOk());
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                    }
                    countDownLatch.countDown();
                });
            });
        }
        countDownLatch.await();
    }

}
