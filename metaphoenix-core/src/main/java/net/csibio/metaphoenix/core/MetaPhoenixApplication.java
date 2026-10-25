package net.csibio.metaphoenix.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableCaching
@EnableScheduling
@EnableAspectJAutoProxy
@ComponentScan(value = "net.csibio.metaphoenix.client")
@ComponentScan(value = "net.csibio.metaphoenix.core")
public class MetaPhoenixApplication {
    public static void main(String[] args) {
        SpringApplication.run(MetaPhoenixApplication.class, args);
    }
}
