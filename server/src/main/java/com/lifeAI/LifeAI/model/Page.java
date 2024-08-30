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
@Table(name = "pages")
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "site_id")
    private Site site;
    private String title;
    @Column(columnDefinition = "bpchar")
    private String metaData;
    @Column(columnDefinition = "bpchar")
    private String data;
    private String url;
}
