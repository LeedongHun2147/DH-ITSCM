package com.example.myhome1.controller;

import com.example.myhome1.model.Mail;
import com.example.myhome1.service.MailService;
import com.example.myhome1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/mail")
public class MailController {

    private final MailService mailService;
    private final UserService userService;

    @Autowired
    public MailController(MailService mailService, UserService userService) {
        this.mailService = mailService;
        this.userService = userService;
    }

    @PostMapping("/send")
    public String sendMail(@RequestParam("to") String to,
                           @RequestParam("subject") String subject,
                           @RequestParam("body") String body) {
        // 현재 로그인한 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 현재 로그인한 사용자의 정보를 사용하여 메일을 전송
        mailService.sendMail(to, subject, body, userService.getUserByUsername(username).getId());
        return "redirect:/mail/success";
    }

    @GetMapping("/success")
    public String successPage() {
        return "mail/success"; // Success page after sending mail
    }

    @GetMapping("/form")
    public String mailForm() {
        return "mail/sendmail"; // Form to compose email
    }

    @GetMapping("/records")
    public String showMailRecords(Model model) {
        List<Mail> mailRecords = mailService.getAllMailRecords(); // 모든 메일 기록 가져오기
        model.addAttribute("mailRecords", mailRecords); // 모델에 이메일 기록 목록 추가
        return "mail/mailRecords"; // 이메일 기록을 보여줄 뷰
    }
}
