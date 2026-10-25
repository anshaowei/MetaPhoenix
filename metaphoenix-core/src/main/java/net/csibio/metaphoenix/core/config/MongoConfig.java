package net.csibio.metaphoenix.core.config;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

import java.util.List;

@Configuration
public class MongoConfig {

    /**
     * Keep the converter created by Spring Boot and only replace its type mapper,
     * so that documents saved under the old MSLibrary package names can still be read.
     */
    @Bean
    public static BeanPostProcessor legacyTypeMapperPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) {
                if (bean instanceof MappingMongoConverter converter) {
                    converter.setTypeMapper(new DefaultMongoTypeMapper(DefaultMongoTypeMapper.DEFAULT_TYPE_KEY, List.of(new LegacyTypeInformationMapper())));
                }
                return bean;
            }
        };
    }
}
