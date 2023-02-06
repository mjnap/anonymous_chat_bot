package com.bot.uni.service;

import com.bot.uni.dao.UserRepository;
import com.bot.uni.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public boolean userExists (String chatId) {
        return userRepository.existsByUserId(chatId);
    }

    public User save (User user) {
        return userRepository.save(user);
    }

    public SendPhoto.SendPhotoBuilder createProfile (User user) {
        String girlProf = "AgACAgQAAxkBAAOpY960ZDi4NhhbBFJ0K8S-iPjIqh0AAky7MRtVq_lS1Q5Q4W_qgmoBAAMCAANzAAMuBA";
        String boyProf = "AgACAgQAAxkBAAOqY960dp2WYcqO_EwKLuKAWDRyEVgAAk27MRtVq_lS3RQ_OAqpxKoBAAMCAANzAAMuBA";
        String text = """
                نام: %s
                سن: %d
                جنسیت: %s
                دانشگاه: %s
                رشته: %s
                ترم: %d
                محل زندگی: %s, %s
                """.formatted(user.getName(),
                user.getAge(),
                user.getSex(),
                user.getUniName(),
                user.getFieldOfStudy(),
                user.getTerm(),
                user.getState(),
                user.getCity());

        String photoPath = (user.getSex().equals("دختر")) ? girlProf : boyProf;
        return SendPhoto.builder()
                .photo(new InputFile(photoPath))
                .caption(text);
    }
}
