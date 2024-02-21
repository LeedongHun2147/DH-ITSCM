package com.example.myhome1.service;

import com.example.myhome1.model.Mail;
import com.example.myhome1.model.User;
import com.example.myhome1.repository.MailRepository;
import com.example.myhome1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MailService {

    private final JavaMailSender javaMailSender;
    private final MailRepository mailRepository;
    private final UserRepository userRepository;

    @Autowired
    public MailService(JavaMailSender javaMailSender, MailRepository mailRepository,UserRepository userRepository) {
        this.javaMailSender = javaMailSender;
        this.mailRepository = mailRepository;
        this.userRepository = userRepository;
    }

    public void sendMail(String recipient, String subject, String body, Long userId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipient); // to를 recipient로 변경
        message.setSubject(subject);
        message.setText(body);

        try {
            javaMailSender.send(message);

            Mail mail = new Mail();
            mail.setRecipient(recipient); // to를 recipient로 변경
            mail.setSubject(subject);
            mail.setBody(body);
            mail.setSendTime(LocalDateTime.now());

            // 사용자 ID 설정
            User user = userRepository.findById(userId).orElse(null); // userRepository 사용
            mail.setUser(user);
            mailRepository.save(mail); // 메일 정보를 데이터베이스에 저장
        } catch (Exception e) {
            // 예외 처리
            e.printStackTrace();
            // 혹은 로그에 에러 기록 등을 수행할 수 있습니다.
        }
    }
    public List<Mail> getAllMailRecords() {
        return mailRepository.findAll(); // 모든 메일 기록 조회
    }
}
