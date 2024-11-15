package com.hmall.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.PostConstruct;

@SpringBootTest
public class TestMysqlConn {
    @Value("${hm.db.host}")
    private String dbHost;

    @PostConstruct
    @Test
    public void init() {
        System.out.println("Database host: " + dbHost);
    }

}
