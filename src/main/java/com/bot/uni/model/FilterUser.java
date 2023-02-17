package com.bot.uni.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class FilterUser {
    private String chatId;
    private Sex sex;
    private Want want;
    private String secretChatId;
}
