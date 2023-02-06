package com.bot.uni.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_table")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class User {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;
    private String userId;
    private String name;
    private Integer age;
    private String sex;
    private String state;
    private String city;
    private String uniName;
    private String fieldOfStudy;
    private Integer term;
}
