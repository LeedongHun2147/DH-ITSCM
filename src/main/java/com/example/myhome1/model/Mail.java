package com.example.myhome1.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class Mail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "recipient") // "to"를 "recipient"로 변경
    private String recipient;

    @Column(name= "subject")
    private String subject;

    @Column(name = "body")
    private String body;

    @Column(name = "send_time") // "sendTime"을 "send_time"으로 변경
    private LocalDateTime sendTime; // LocalDateTime으로 수정

    // Getter와 Setter 메서드

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }
}
