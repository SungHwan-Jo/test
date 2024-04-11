package com.jsh.test.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ThreadSleepService {
    private final static Logger logger = LogManager.getLogger(ThreadSleepService.class);
    @Value("${service_time}")
    private String service_time;

    public void threadSleep_start(){
        try{
            logger.info("################### Thread Sleep Start ###################");
            Thread.sleep(Integer.parseInt(service_time));
        }catch(InterruptedException e){
            logger.error(e.getMessage());
        }
    }

    public void threadSleep_stop(){
        logger.info("################### Thread Sleep Stop ###################");
        Thread.interrupted();
    }
}
