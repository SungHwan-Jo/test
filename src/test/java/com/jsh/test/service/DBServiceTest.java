package com.jsh.test.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DBServiceTest {

    @Autowired
    DBService dbService;
    @Test
    public void dbconCheck(){
        String result = dbService.dbconCheck();
        System.out.println(result);
    }

}