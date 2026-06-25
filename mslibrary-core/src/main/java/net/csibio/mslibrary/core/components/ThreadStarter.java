package net.csibio.mslibrary.core.components;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "command.runner.enabled", havingValue = "true")
public class ThreadStarter implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Thread thread = new Thread(event.getApplicationContext().getBean(CommandRunner.class));
        thread.start();
    }

}
