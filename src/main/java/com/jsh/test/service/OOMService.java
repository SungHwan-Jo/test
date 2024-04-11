package com.jsh.test.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Stack;
import java.util.stream.IntStream;

@Service
public class OOMService {
    private final static Logger logger = LogManager.getLogger(ThreadSleepService.class);

    //OOM Sample code
    public void oom_start(){
        logger.info("################### OOM TEST Start ###################");
        Stack stack = new Stack<String>();
        int mb = 1024*1024;
        Runtime runtime = Runtime.getRuntime();

        logger.info("Free Memory = {} Max Memory = {} totalMemory = {}",
                    runtime.freeMemory() / mb, runtime.maxMemory() / mb,
                    runtime.totalMemory() / mb);
        for(int i=1; i < Integer.MAX_VALUE; i++){
            stack.push(i);
        }

    }
}
