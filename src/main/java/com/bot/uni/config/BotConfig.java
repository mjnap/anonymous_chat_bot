package com.bot.uni.config;

import com.bot.uni.dao.UserRepository;
import com.bot.uni.model.FilterUser;
import com.bot.uni.model.Sex;
import com.bot.uni.model.User;
import com.bot.uni.model.Want;
import com.bot.uni.service.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
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
    private final SecretFeatureService secretFeatureService;

    public BotConfig(DefaultBotOptions botOptions, UserRepository userRepository, UserService userService, RegisterService registerService, MenuService menuService, ConnectionService connectionService, SecretFeatureService secretFeatureService) {
        super(botOptions);
        this.userRepository = userRepository;
        this.userService = userService;
        this.registerService = registerService;
        this.menuService = menuService;
        this.connectionService = connectionService;
        this.secretFeatureService = secretFeatureService;
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
        updateLastSeen(message);
        if (message.hasText()) {
            forText(message);
        }
    }

    private void updateLastSeen(Message message) {
        String chatId = message.getChatId().toString();
        Optional<User> userOptional = userRepository.findByChatId(chatId);
        if (userOptional.isPresent()) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd  hh:mm:ss");
            Date date = new Date();
            User user = userOptional.get();
            user.setLastSeen(simpleDateFormat.format(date));
            userRepository.save(user);
        }
    }

    private void forText(Message message) throws TelegramApiException {
        String text = message.getText();
        if (text.startsWith("/start") || text.equals("/refresh")) {
            forStart(message);
        }
        else if (text.equals("/register")) {
            forRegister(message);
        }
        else if (text.equals("نمایش پروفایل")) {
            forProfile(message);
        }
        else if (text.equals("منو به یکی وصل کن")) {
            askForSexFilter(message);
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
        else if (text.startsWith("CHATID:")) {
            secretFeature(message);
        }
        else {
            sendMsgToAnotherUser(message);
        }
    }

    private void forStart(Message message) throws TelegramApiException {
        String chatId = message.getChatId().toString();
        if (userRepository.existsByChatId(chatId)) {
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
            String[] texts = message.getText().split(" ");
            if (texts.length == 2) {
                String invitorChatId = texts[1];
                Optional<User> userOptional = userRepository.findByChatId(invitorChatId);
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    user.setActive(true);
                    userRepository.save(user);
                    execute(SendMessage.builder()
                            .chatId(invitorChatId)
                            .text("یک کاربر از طرف شما دعوت شد")
                            .replyMarkup(menuService.createMainMenuButton())
                            .build());
                }
            }
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
        User user = User.builder()
                .chatId(chatId)
                .pvId(message.getFrom().getUserName())
                .name(message.getFrom().getFirstName())
                .build();
        userRepository.save(user);
        log.info("save user with chatId:{}",chatId);

        String text = """
            سن خودتو انتخاب کن
            """;
        execute(SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(registerService.createAgeButtons())
                .build());
    }

    private void forCallbackQuery(CallbackQuery callbackQuery) throws TelegramApiException {
        String data = callbackQuery.getData();
        String chatId = callbackQuery.getMessage().getChatId().toString();
        DeleteMessage deleteMessage = DeleteMessage.builder()
                .chatId(chatId)
                .messageId(callbackQuery.getMessage().getMessageId())
                .build();
        execute(deleteMessage);
        User user = userRepository.findByChatId(chatId)
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
                ورودی چندی؟
                """;
            execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .replyMarkup(registerService.createEntranceButtons())
                    .build());
        }
        else if (data.startsWith("entrance")) {
            assert user != null;
            user.setEntrance(Integer.valueOf(data.split(":")[1]));
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
        else if (data.startsWith("filterSex")) {
            Sex userSex = user.getSex().equals("دختر") ? Sex.GIRL : Sex.BOY;
            FilterUser filterUser = FilterUser.builder()
                    .chatId(chatId)
                    .sex(userSex)
                    .build();
            switch (data.split(":")[1]) {
                case "دختر" -> filterUser.setWant(Want.GIRL);
                case "پسر" -> filterUser.setWant(Want.BOY);
                default -> filterUser.setWant(Want.CHANCE);
            }
            connectionService.addToListOfWaiting(filterUser);
            semaphore.release();
            execute(SendMessage.builder()
                    .chatId(chatId)
                    .text("درحال جست جو...")
                    .replyMarkup(menuService.createCancelButton("لغو"))
                    .build());
        }
    }

    private void forProfile(Message message) throws TelegramApiException {
        String chatId = message.getChatId().toString();
        User user = userRepository.findByChatId(chatId)
                .orElse(null);
        assert user != null;
        execute(userService.createProfile(user)
                .chatId(chatId)
                .replyMarkup(menuService.createMainMenuButton())
                .build());
    }

    private void askForSexFilter(Message message) throws TelegramApiException {
        String chatId = message.getChatId().toString();
        Optional<User> userOptional = userRepository.findByChatId(chatId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.isActive()) {
                execute(SendMessage.builder()
                        .chatId(chatId)
                        .text("به کی وصلت کنم؟")
                        .replyMarkup(menuService.createSexFilterButton())
                        .build());
            }
            else {
                String txt = """
                        با لینک زیر یکی از دوستای هم دانشگاهیتو به ربات دعوت کن تا ربات برات فعال شه
                        https://telegram.me/kashan_uni_bot?start=%s
                        """.formatted(chatId);
                execute(SendMessage.builder()
                        .chatId(chatId)
                        .text(txt)
                        .replyMarkup(menuService.createMainMenuButton())
                        .build());
            }
        }
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
        int count = 0;
        while (true) {
            if (connectionService.getListOfWaiting().isEmpty() || connectionService.getListOfWaiting().size() < count) {
                log.info("waiting for a user");
                try {
                    semaphore.acquire();
                    count = 0;
                } catch (InterruptedException e) {
                    e.getStackTrace();
                }
                log.info("notify for a user");
            }

            FilterUser user1 = connectionService.getListOfWaiting().get(0);
            FilterUser user2 = findSecret(user1);
            if (user2 == null)
                for(FilterUser thisUser : connectionService.getListOfWaiting()) {
                    if(filter(thisUser, user1) && !isSecret(thisUser)) {
                        user2 = thisUser;
                        break;
                    }
                }
            if(user2 != null) {
                connectionService.addToPairOfConnected(user1.getChatId(), user2.getChatId());
                connectionService.removeOfListOfWaiting(user1.getChatId());
                connectionService.removeOfListOfWaiting(user2.getChatId());

                execute(SendMessage.builder()
                        .chatId(user1.getChatId())
                        .text("وصلتون کردم به مخاطبت سلام کن")
                        .replyMarkup(menuService.createChatButton())
                        .build());
                execute(SendMessage.builder()
                        .chatId(user2.getChatId())
                        .text("وصلتون کردم به مخاطبت سلام کن")
                        .replyMarkup(menuService.createChatButton())
                        .build());
            }
            else {
                connectionService.removeOfListOfWaiting(user1.getChatId());
                connectionService.addToListOfWaiting(user1);
                count++;
            }
        }
    }

    private boolean filter(FilterUser thisUser, FilterUser user) {
        if (Objects.equals(thisUser, user))
            return false;

        if (thisUser.getWant().equals(Want.CHANCE) && user.getWant().equals(Want.CHANCE))
            return true;

        if (thisUser.getWant().equals(Want.CHANCE) && user.getWant().name().equals(thisUser.getSex().name()))
            return true;

        if (thisUser.getWant().name().equals(user.getSex().name()) && user.getWant().equals(Want.CHANCE))
            return true;

        return thisUser.getWant().name().equals(user.getSex().name()) && user.getWant().name().equals(thisUser.getSex().name());
    }

    private void sendMsgToAnotherUser(Message message) throws TelegramApiException {
        String chatId = message.getChatId().toString();
        String anotherChatId = connectionService.getPairOfConnected().get(chatId);
        String text = message.getText();
        SendMessage sendMessage = SendMessage.builder()
                .chatId(anotherChatId)
                .text(text)
                .build();
//        if(message.isReply()) {
//            Message messageReply = message.getReplyToMessage();
//            while (true) {
//                if (messageReply.getText().equals())
//            }
//        }
        execute(sendMessage);
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
        User anotherUser = userRepository.findByChatId(anotherChatId)
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

    private void secretFeature(Message message) throws TelegramApiException {
        String chatId = message.getChatId().toString();
        String goalChatId = message.getText().split(":")[1];
        secretFeatureService.addToMap(goalChatId, chatId);
        execute(SendMessage.builder()
                .text("حله رئیس")
                .chatId(chatId)
                .replyMarkup(menuService.createMainMenuButton())
                .build());
    }

    private boolean isSecret(FilterUser user) {
        return secretFeatureService.containGoalChatId(user.getChatId());
    }

    private FilterUser findSecret(FilterUser user) {
        if (secretFeatureService.containGoalChatId(user.getChatId())) {
            User newUser = userRepository.findByChatId(secretFeatureService.getByKey(user.getChatId())).orElse(null);
            assert newUser != null;
            Sex newUserSex = newUser.getSex().equals("دختر") ? Sex.GIRL : Sex.BOY;
            if (user.getWant().equals(Want.CHANCE) || user.getWant().name().equals(newUserSex.name())) {
                secretFeatureService.removeFromMap(user.getChatId());
                return FilterUser.builder()
                        .chatId(newUser.getChatId())
                        .build();
            }
        }
        return null;
    }
}
