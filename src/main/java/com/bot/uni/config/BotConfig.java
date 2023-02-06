package com.bot.uni.config;

import com.bot.uni.dao.UserRepository;
import com.bot.uni.model.User;
import com.bot.uni.service.ConnectionService;
import com.bot.uni.service.MenuService;
import com.bot.uni.service.RegisterService;
import com.bot.uni.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@Configuration
@Slf4j
public class BotConfig extends TelegramLongPollingBot {

    private final Executor executor = Executors.newCachedThreadPool();
    private final Semaphore semaphore = new Semaphore(0);
    private final UserRepository userRepository;
    private final UserService userService;
    private final RegisterService registerService;
    private final MenuService menuService;
    private final ConnectionService connectionService;

    public BotConfig(DefaultBotOptions botOptions, UserRepository userRepository, UserService userService, RegisterService registerService, MenuService menuService, ConnectionService connectionService) {
        super(botOptions);
        this.userRepository = userRepository;
        this.userService = userService;
        this.registerService = registerService;
        this.menuService = menuService;
        this.connectionService = connectionService;
    }

    @Override
    public String getBotUsername() {
        return "kashan_uni_bot";
    }

    @Override
    public String getBotToken() {
        return "6026214243:AAHmgSwwXBJJkM73XXevV2pJ6drbOOmI1MQ";
    }

    @Override
    public void onUpdateReceived(Update update) {
        executor.execute(() -> {
            if (update.hasMessage()) {
                try {
                    forMessage(update.getMessage());
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
            else if (update.hasCallbackQuery()){
                try {
                    forCallbackQuery(update.getCallbackQuery());
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void forMessage(Message message) throws TelegramApiException {
        if (message.hasText()) {
            forText(message);
        }
    }

    private void forText(Message message) throws TelegramApiException {
        String exText = "";
        if (message.getReplyToMessage() != null)
            exText = message.getReplyToMessage().getText();
        String text = message.getText();
        if (text.equals("/start")) {
            forStart(message);
        }
        else if (text.equals("/register") || exText.startsWith("نام")) {
            forRegister(message);
        }
        else if (text.equals("نمایش پروفایل")) {
            forProfile(message);
        }
        else if (text.equals("منو به یکی وصل کن")) {
            connection(message);
        }
        else if (text.equals("لغو")) {
            forCancelSearching(message);
        }
        else if (text.equals("پایان چت")) {
            askForSure(message);
        }
        else if (text.equals("نمایش پروفایل کاربر")) {
            forAnotherProfile(message);
        }
        else {
            sendMsgToAnotherUser(message);
        }
    }

    private void forStart(Message message) throws TelegramApiException {
        String chatId = message.getChatId().toString();
        if (userRepository.existsByUserId(chatId)) {
            String text = """
                    چه کاری برات انجام بدم؟
                    """;
            execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .replyMarkup(menuService.createMainMenuButton())
                    .build());
        }
        else {
            String text = """
                    به ربات چت ناشناس دانشگاه کاشان خوش اومدی
                    برای اینکه بتونی از ربات استفاده کنی باید پروفایل خودتو تکمیل کنی
                    /register
                    """;
            execute(new SendMessage(chatId, text));
        }
    }

    private void forRegister(Message message) throws TelegramApiException {
        String chatId = message.getChatId().toString();
        User user = userRepository.findByUserId(chatId)
                .orElse(null);
        if (message.getText().equals("/register")) {
            user = User.builder()
                    .userId(chatId)
                    .build();
            userRepository.save(user);
            log.info("save user with chatId:{}",chatId);

            String text = """
                نام خودتو وارد کن (روی این پیام ریپلای کن)
                """;
            execute(new SendMessage(chatId, text));
        }
        else {
            assert user != null;
            user.setName(message.getText());
            userRepository.save(user);
            log.info("update user name with chatId:{}",chatId);

            String text = """
                سن خودتو انتخاب کن
                """;
            execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .replyMarkup(registerService.createAgeButtons())
                    .build());
        }
    }

    private void forCallbackQuery(CallbackQuery callbackQuery) throws TelegramApiException {
        String data = callbackQuery.getData();
        String chatId = callbackQuery.getMessage().getChatId().toString();
        DeleteMessage deleteMessage = DeleteMessage.builder()
                .chatId(chatId)
                .messageId(callbackQuery.getMessage().getMessageId())
                .build();
        execute(deleteMessage);
        User user = userRepository.findByUserId(chatId)
                .orElse(null);
        if (data.startsWith("age")) {
            assert user != null;
            user.setAge(Integer.valueOf(data.split(":")[1]));
            userRepository.save(user);
            log.info("update user age with chatId:{}", chatId);

            String text = """
                جنسیت خودتو انتخاب کن
                """;
            execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .replyMarkup(registerService.createSexButtons())
                    .build());
        }
        else if (data.startsWith("sex")) {
            assert user != null;
            user.setSex(data.split(":")[1]);
            userRepository.save(user);
            log.info("update user sex with chatId:{}", chatId);

            String text = """
                کدوم دانشگاه درس میخونی؟
                """;
            execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .replyMarkup(registerService.createUniButtons())
                    .build());
        }
        else if (data.startsWith("uni")) {
            assert user != null;
            user.setUniName(data.split(":")[1]);
            userRepository.save(user);
            log.info("update user uni with chatId:{}", chatId);

            String text = """
                چه رشته ای میخونی؟
                """;
            execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .replyMarkup(registerService.createFieldOfStudyButtons(data.split(":")[1]))
                    .build());
        }
        else if (data.startsWith("field")) {
            assert user != null;
            user.setFieldOfStudy(data.split(":")[1]);
            userRepository.save(user);
            log.info("update user field of study with chatId:{}", chatId);

            String text = """
                ترم چندی؟
                """;
            execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .replyMarkup(registerService.createTermButtons())
                    .build());
        }
        else if (data.startsWith("term")) {
            assert user != null;
            user.setTerm(Integer.valueOf(data.split(":")[1]));
            userRepository.save(user);
            log.info("update user term with chatId:{}", chatId);

            String text = """
                کدوم استان زندگی میکنی؟
                """;
            execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .replyMarkup(registerService.createStateButtons())
                    .build());
        }
        else if (data.startsWith("state")) {
            assert user != null;
            user.setState(data.split(":")[1]);
            userRepository.save(user);
            log.info("update user state with chatId:{}", chatId);

            String text = """
                کدوم شهر زندگی میکنی؟
                """;
            execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .replyMarkup(registerService.createCityButtons(data.split(":")[1]))
                    .build());
        }
        else if (data.startsWith("city")) {
            assert user != null;
            user.setCity(data.split(":")[1]);
            userRepository.save(user);
            log.info("update user city with chatId:{}", chatId);

            String text = """
                ثبت نام شما کامل شد
                """;
            execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .replyMarkup(menuService.createMainMenuButton())
                    .build());
        }
        else if (data.startsWith("suer")) {
            if (data.split(":")[1].equals("اره"))
                forCancelChat(callbackQuery.getMessage());
            else {
                execute(SendMessage.builder()
                        .chatId(chatId)
                        .text("به چتتون ادامه بدید")
                        .replyMarkup(menuService.createChatButton())
                        .build());
            }
        }
    }

    private void forProfile(Message message) throws TelegramApiException {
        String chatId = message.getChatId().toString();
        User user = userRepository.findByUserId(chatId)
                .orElse(null);
        assert user != null;
        execute(userService.createProfile(user)
                .chatId(chatId)
                .replyMarkup(menuService.createMainMenuButton())
                .build());
    }

    private void connection(Message message) throws TelegramApiException {
        String chatId = message.getChatId().toString();
        connectionService.addToListOfWaiting(chatId);
        semaphore.release();
        execute(SendMessage.builder()
                .chatId(chatId)
                .text("درحال جستجو...")
                .replyMarkup(menuService.createCancelButton("لغو"))
                .build());
    }

    @PostConstruct
    private void init() {
        executor.execute(() -> {
            try {
                connector();
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private synchronized void connector() throws TelegramApiException {
        while (true) {
            if (connectionService.getListOfWaiting().isEmpty()) {
                log.debug("waiting for a user");
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    e.getStackTrace();
                }
                log.debug("notify for a user");
            }

            String chatId1 = connectionService.getListOfWaiting().stream()
                    .findFirst()
                    .orElse(null);
            String chatId2 = connectionService.getListOfWaiting().stream()
                    .filter(chatId -> !Objects.equals(chatId, chatId1))
                    .findFirst()
                    .orElse(null);
            if(chatId2 != null) {
                connectionService.addToPairOfConnected(chatId1, chatId2);
                connectionService.removeOfListOfWaiting(chatId1);
                connectionService.removeOfListOfWaiting(chatId2);

                execute(SendMessage.builder()
                        .chatId(chatId1)
                        .text("وصلتون کردم به مخاطبت سلام کن")
                        .replyMarkup(menuService.createChatButton())
                        .build());
                execute(SendMessage.builder()
                        .chatId(chatId2)
                        .text("وصلتون کردم به مخاطبت سلام کن")
                        .replyMarkup(menuService.createChatButton())
                        .build());
            }
        }
    }

    private void sendMsgToAnotherUser(Message message) throws TelegramApiException {
        String chatId = message.getChatId().toString();
        String anotherChatId = connectionService.getPairOfConnected().get(chatId);
        String text = message.getText();
        execute(SendMessage.builder()
                .chatId(anotherChatId)
                .text(text)
                .build());
    }

    private void forCancelSearching(Message message) throws TelegramApiException {
        String chatId = message.getChatId().toString();
        connectionService.removeOfListOfWaiting(chatId);
        execute(SendMessage.builder()
                .chatId(chatId)
                .text("جستجو لغو شد")
                .replyMarkup(menuService.createMainMenuButton())
                .build());
    }

    private void askForSure(Message message) throws TelegramApiException {
        String chatId = message.getChatId().toString();
        execute(SendMessage.builder()
                .chatId(chatId)
                .text("مطمئنی میخوای چتو قطع کنی؟")
                .replyMarkup(menuService.createForSureButton())
                .build());
    }

    private void forCancelChat(Message message) throws TelegramApiException {
        String chatId = message.getChatId().toString();
        String anotherChatId = connectionService.getPairOfConnected().get(chatId);
        connectionService.removeOfPairOfConnected(chatId, anotherChatId);

        execute(SendMessage.builder()
                .chatId(anotherChatId)
                .text("چت از طرف کاربر مقابل بسته شد")
                .replyMarkup(menuService.createMainMenuButton())
                .build());

        execute(SendMessage.builder()
                .chatId(chatId)
                .text("چت از طرف شما بسته شد")
                .replyMarkup(menuService.createMainMenuButton())
                .build());
    }

    private void forAnotherProfile(Message message) throws TelegramApiException {
        String chatId = message.getChatId().toString();
        String anotherChatId = connectionService.getPairOfConnected().get(chatId);
        User anotherUser = userRepository.findByUserId(anotherChatId)
                .orElse(null);
        assert anotherUser != null;
        execute(userService.createProfile(anotherUser)
                .chatId(chatId)
                .replyMarkup(menuService.createChatButton())
                .build());
        execute(SendMessage.builder()
                .chatId(anotherChatId)
                .text("کابر مقابل پروفایل شما رو مشاهده کرد")
                .replyMarkup(menuService.createChatButton())
                .build());
    }
}
