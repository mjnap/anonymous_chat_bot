package com.bot.uni.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class FieldOfStudy {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;
    private String uni;
    private String name;
}
