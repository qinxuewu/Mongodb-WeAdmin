package com.qxw.utils;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 配置
 * @author qxw
 * @data 2018年11月19日上午10:48:28
 */
public class CommonApi {
    private static final Logger logger = LoggerFactory.getLogger(CommonApi.class);
    public static String mongo_host_port;
    public static String mongo_user_pass_db;


    static {
        try {
            InputStream is = CommonApi.class.getResourceAsStream("/application.properties");
            Properties properties = new Properties();
            properties.load(is);
            mongo_host_port = properties.getProperty("mongo.host.port");
            mongo_user_pass_db = properties.getProperty("mongo.user.pass.db");
            is.close();
        } catch (Exception ex) {
            logger.debug("加载配置文件出错：" + ex.getMessage());
        }
    }
}
