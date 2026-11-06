package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration(proxyBeanMethods = false)
public class FileUploadConfig {

    /**
     * 为对象键提供按站点时区划分日期的时钟。
     *
     * @return 文件上传使用的时钟
     */
    @Bean
    public Clock fileUploadClock() {
        return Clock.system(ZoneId.of("Asia/Shanghai"));
    }
}
