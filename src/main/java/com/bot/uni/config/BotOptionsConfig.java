package com.bot.uni.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@Configuration
public class BotOptionsConfig {

    @Bean
    public DefaultBotOptions botOptions() {
        DefaultBotOptions botOptions = new DefaultBotOptions();
        botOptions.setProxyType(DefaultBotOptions.ProxyType.HTTP);
        botOptions.setProxyHost("127.0.0.1");
        botOptions.setProxyPort(10809);
        return botOptions;
    }
}
