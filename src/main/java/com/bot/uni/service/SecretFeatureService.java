package com.bot.uni.service;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Getter
public class SecretFeatureService {
    private Map<String, String> chatIds = new HashMap<>();

    public void addToMap(String goalChatId, String thisChatId) {
        chatIds.put(goalChatId, thisChatId);
    }

    public void removeFromMap(String goalChatId) {
        chatIds.remove(goalChatId);
    }

    public boolean containGoalChatId(String goalChatId) {
        return chatIds.containsKey(goalChatId);
    }

    public String getByKey(String key) {
        return chatIds.get(key);
    }
}
