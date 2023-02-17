package com.bot.uni.service;

import com.bot.uni.model.FilterUser;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@Getter
public class ConnectionService {

    private final List<FilterUser> listOfWaiting = new Vector<>();
    private final Map<String, String> pairOfConnected = new ConcurrentHashMap<>();

    public void addToListOfWaiting(FilterUser filterUser) {
        listOfWaiting.add(filterUser);
        log.info("add user:{} to list of waiting", filterUser.getChatId());
    }

    public void removeOfListOfWaiting(String chatId) {
        Optional<FilterUser> filterUser = listOfWaiting.stream().filter(user -> user.getChatId().equals(chatId)).findFirst();
        if (filterUser.isPresent()) {
            listOfWaiting.remove(filterUser.get());
            log.info("remove user:{} of list of waiting", chatId);
        }
    }

    public void addToPairOfConnected(String chatId1, String chatId2) {
        pairOfConnected.put(chatId1, chatId2);
        pairOfConnected.put(chatId2, chatId1);
        log.info("Connect user:{} and user:{}", chatId1, chatId2);
    }

    public void removeOfPairOfConnected(String chatId1, String chatId2) {
        pairOfConnected.remove(chatId1);
        pairOfConnected.remove(chatId2);
        log.info("Disconnect user:{} and user:{}", chatId1, chatId2);
    }
}
