package com.inspur.health.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.util.Properties;

public class PropertiesUtils {

    private static Properties properties = readPropertiesFile();

    /**
     * 通过配置文件名读取内容
     * @return
     */
    public static Properties readPropertiesFile() {
        try {
            Resource resource = new ClassPathResource("conf.properties");
            Properties props = PropertiesLoaderUtils.loadProperties(resource);
            return props;
        } catch (Exception e) {
            System.out.println("————读取配置文件：" + "conf.properties" + "出现异常，读取失败————");
            e.printStackTrace();
        }
        return null;
    }

    public static String getProp(String key) {
        return properties.getProperty(key);
    }
}
