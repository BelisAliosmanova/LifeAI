package com.lifeAI.LifeAI.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    private String text;
    private Long dependsOn; // ID of the triggering question
    private String dependsOnAnswer; // Answer ("YES" or "NO") that triggers this question
    private String result;
}
