package com.qxw.mongodb;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * mongo配置
 * @author qinxuewu
 * @version 1.00
 * @time  29/11/2018 下午 1:34
 * @email 870439570@qq.com
 */
@Configuration
public class MongoConfig {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Value("${mongo.host.port}")
    private  String HOST_PORT;
    @Value("${mongo.user.pass.db}")
    private  String USER_PASS_DB;

    @Bean
    public MongoClient mongoClient(){
        logger.debug("*********初始化mongodb*************************");
        MongoClient mongoClient=null;
        List<MongoCredential> mongoCredential=Collections.<MongoCredential>emptyList();
        if(!StringUtils.isEmpty(USER_PASS_DB)){
            //arr 0,1,2 用户名  密码  数据名
            String[] arr = USER_PASS_DB.split(":");
            MongoCredential credential = MongoCredential.createScramSha1Credential(arr[0],arr[2],arr[1].toCharArray());
            mongoCredential=new ArrayList<MongoCredential>();
            mongoCredential.add(credential);
        }
        MongoClientOptions option=new MongoClientOptions.Builder().threadsAllowedToBlockForConnectionMultiplier(10).build();
        String[] hostps = HOST_PORT.split(";");
        if (hostps.length == 1) {
            //只有一个主 副本集
            String[] h = hostps[0].split(":");
            mongoClient = new MongoClient(new ServerAddress(h[0], Integer.parseInt(h[1])),mongoCredential,option);
        } else {
            List<ServerAddress> serverAddress = new ArrayList<ServerAddress>();
            for (String hp : hostps) {
                String[] h = hp.split(":");
                serverAddress.add(new ServerAddress(h[0], Integer.parseInt(h[1])));
            }
            mongoClient = new MongoClient(serverAddress, mongoCredential,option);

        }
        logger.info("*********** mongoClient 偏好为=「」" + mongoClient.getReadPreference().toString());
        return  mongoClient;
    }

}
