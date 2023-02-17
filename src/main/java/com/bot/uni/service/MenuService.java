package com.bot.uni.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Service
public class MenuService {

    public ReplyKeyboardMarkup createMainMenuButton () {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);

        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();

        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setText("منو به یکی وصل کن");
        keyboardRow.add(keyboardButton);
        keyboardRowList.add(keyboardRow);

        keyboardButton = new KeyboardButton();
        keyboardRow = new KeyboardRow();
        keyboardButton.setText("نمایش پروفایل");
        keyboardRow.add(keyboardButton);
        keyboardRowList.add(keyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup createCancelButton (String text) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);

        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();

        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setText(text);
        keyboardRow.add(keyboardButton);
        keyboardRowList.add(keyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup createChatButton() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);

        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();

        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setText("نمایش پروفایل کاربر");
        keyboardRow.add(keyboardButton);
        keyboardRowList.add(keyboardRow);

        keyboardButton = new KeyboardButton();
        keyboardRow = new KeyboardRow();
        keyboardButton.setText("پایان چت");
        keyboardRow.add(keyboardButton);
        keyboardRowList.add(keyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        return replyKeyboardMarkup;
    }

    public InlineKeyboardMarkup createForSureButton() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();
        List<InlineKeyboardButton> inlineKeyboardButtonList = new ArrayList<>();

        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("اره");
        inlineKeyboardButton.setCallbackData("suer:اره");
        inlineKeyboardButtonList.add(inlineKeyboardButton);

        inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("نه قطع نکن");
        inlineKeyboardButton.setCallbackData("suer:نه");
        inlineKeyboardButtonList.add(inlineKeyboardButton);

        inlineButtons.add(inlineKeyboardButtonList);
        inlineKeyboardMarkup.setKeyboard(inlineButtons);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup createSexFilterButton() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();
        List<InlineKeyboardButton> inlineKeyboardButtonList = new ArrayList<>();

        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("دختر");
        inlineKeyboardButton.setCallbackData("filterSex:دختر");
        inlineKeyboardButtonList.add(inlineKeyboardButton);

        inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("پسر");
        inlineKeyboardButton.setCallbackData("filterSex:پسر");
        inlineKeyboardButtonList.add(inlineKeyboardButton);
        inlineButtons.add(inlineKeyboardButtonList);

        inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButtonList = new ArrayList<>();
        inlineKeyboardButton.setText("مهم نیست");
        inlineKeyboardButton.setCallbackData("filterSex:مهم نیست");
        inlineKeyboardButtonList.add(inlineKeyboardButton);
        inlineButtons.add(inlineKeyboardButtonList);

        inlineKeyboardMarkup.setKeyboard(inlineButtons);
        return inlineKeyboardMarkup;
    }
}
