package com.jsh.test.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class DBService {
    private final static Logger logger = LogManager.getLogger(DBService.class);
    @Value("${db_url}")
    private String db_url;

    public String dbconCheck(){
        String result="";
        String urlStr = db_url;
        try{
            URL url = new URL(urlStr);
            HttpURLConnection urlcon = (HttpURLConnection)url.openConnection();
            if(urlcon != null){
                urlcon.setConnectTimeout(10000);
                urlcon.setRequestMethod("GET");
                urlcon.setDoInput(true);

                int resCode = urlcon.getResponseCode();
                if(resCode == HttpURLConnection.HTTP_OK){
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlcon.getInputStream()));
                    String line;
                    while(true) {
                        line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        result = result + line;
                    }
                    reader.close();
                }
                urlcon.disconnect();
            }
        }catch (Exception e){
            logger.error("#DBSErvice#" + e.getMessage());
        }

        return result;
    }
}
