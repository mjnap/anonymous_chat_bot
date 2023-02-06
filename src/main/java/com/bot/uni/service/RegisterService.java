package com.bot.uni.service;

import com.bot.uni.dao.CityRepository;
import com.bot.uni.dao.FieldOfStudyRepository;
import com.bot.uni.dao.StateRepository;
import com.bot.uni.model.City;
import com.bot.uni.model.FieldOfStudy;
import com.bot.uni.model.State;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final FieldOfStudyRepository fieldOfStudyRepository;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;

    public InlineKeyboardMarkup createAgeButtons () {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();
        List<InlineKeyboardButton> inlineKeyboardButtonList = new ArrayList<>();
        for (int i=16; i<=35; i++) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(String.valueOf(i));
            inlineKeyboardButton.setCallbackData("age:"+i);
            inlineKeyboardButtonList.add(inlineKeyboardButton);
            if (inlineKeyboardButtonList.size() == 5) {
                inlineButtons.add(inlineKeyboardButtonList);
                inlineKeyboardButtonList = new ArrayList<>();
            }
        }
        inlineKeyboardMarkup.setKeyboard(inlineButtons);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup createSexButtons () {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();

        List<InlineKeyboardButton> inlineKeyboardButtonList = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("دختر");
        inlineKeyboardButton.setCallbackData("sex:دختر");
        inlineKeyboardButtonList.add(inlineKeyboardButton);

        inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("پسر");
        inlineKeyboardButton.setCallbackData("sex:پسر");
        inlineKeyboardButtonList.add(inlineKeyboardButton);
        inlineButtons.add(inlineKeyboardButtonList);

        inlineKeyboardMarkup.setKeyboard(inlineButtons);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup createUniButtons () {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();

        List<InlineKeyboardButton> inlineKeyboardButtonList = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("دولتی کاشان");
        inlineKeyboardButton.setCallbackData("uni:دولتی");
        inlineKeyboardButtonList.add(inlineKeyboardButton);

        inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("علوم پزشکی کاشان");
        inlineKeyboardButton.setCallbackData("uni:علوم");
        inlineKeyboardButtonList.add(inlineKeyboardButton);
        inlineButtons.add(inlineKeyboardButtonList);

        inlineKeyboardMarkup.setKeyboard(inlineButtons);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup createFieldOfStudyButtons (String uni) {
        List<String> fields = fieldOfStudyRepository.findAllByUni(uni).stream()
                .map(FieldOfStudy::getName)
                .toList();

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();
        List<InlineKeyboardButton> inlineKeyboardButtonList = new ArrayList<>();
        for (String field : fields) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(field);
            inlineKeyboardButton.setCallbackData("field:"+field);
            inlineKeyboardButtonList.add(inlineKeyboardButton);
            if (inlineKeyboardButtonList.size() == 3 || fields.get(fields.size()-1).equals(field)) {
                inlineButtons.add(inlineKeyboardButtonList);
                inlineKeyboardButtonList = new ArrayList<>();
            }
        }

        inlineKeyboardMarkup.setKeyboard(inlineButtons);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup createTermButtons () {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();
        List<InlineKeyboardButton> inlineKeyboardButtonList = new ArrayList<>();
        for (int i=1; i<=8; i++) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(String.valueOf(i));
            inlineKeyboardButton.setCallbackData("term:"+i);
            inlineKeyboardButtonList.add(inlineKeyboardButton);
            if (inlineKeyboardButtonList.size() == 4) {
                inlineButtons.add(inlineKeyboardButtonList);
                inlineKeyboardButtonList = new ArrayList<>();
            }

            if (i==8) {
                inlineKeyboardButton = new InlineKeyboardButton();
                inlineKeyboardButton.setText("8+");
                inlineKeyboardButton.setCallbackData("term:"+9);
                inlineKeyboardButtonList.add(inlineKeyboardButton);
                inlineButtons.add(inlineKeyboardButtonList);
            }
        }

        inlineKeyboardMarkup.setKeyboard(inlineButtons);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup createStateButtons () {
        List<String> states = stateRepository.findAll().stream()
                .map(State::getName)
                .toList();

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();
        List<InlineKeyboardButton> inlineKeyboardButtonList = new ArrayList<>();
        for (String state : states) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(state);
            inlineKeyboardButton.setCallbackData("state:"+state);
            inlineKeyboardButtonList.add(inlineKeyboardButton);
            if (inlineKeyboardButtonList.size() == 3 || states.get(states.size()-1).equals(state)) {
                inlineButtons.add(inlineKeyboardButtonList);
                inlineKeyboardButtonList = new ArrayList<>();
            }
        }

        inlineKeyboardMarkup.setKeyboard(inlineButtons);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup createCityButtons (String stateName) {
        State state = stateRepository.findByName(stateName).orElse(null);
        assert state != null;
        List<String> cities = cityRepository.findAllByState_Id(state.getId()).stream()
                .map(City::getName)
                .toList();

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();
        List<InlineKeyboardButton> inlineKeyboardButtonList = new ArrayList<>();
        for (String city : cities) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(city);
            inlineKeyboardButton.setCallbackData("city:"+city);
            inlineKeyboardButtonList.add(inlineKeyboardButton);
            if (inlineKeyboardButtonList.size() == 3 || cities.get(cities.size()-1).equals(city)) {
                inlineButtons.add(inlineKeyboardButtonList);
                inlineKeyboardButtonList = new ArrayList<>();
            }
        }

        inlineKeyboardMarkup.setKeyboard(inlineButtons);
        return inlineKeyboardMarkup;
    }
}
