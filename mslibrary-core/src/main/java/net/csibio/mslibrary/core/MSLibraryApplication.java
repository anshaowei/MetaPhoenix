package net.csibio.mslibrary.core;

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
@ComponentScan(value = "net.csibio.mslibrary.client")
@ComponentScan(value = "net.csibio.mslibrary.core")
public class MSLibraryApplication {
    public static void main(String[] args) {
        SpringApplication.run(MSLibraryApplication.class, args);
    }
}
