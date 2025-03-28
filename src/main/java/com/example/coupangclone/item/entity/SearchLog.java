package com.example.coupangclone.item.entity;

import com.example.coupangclone.global.common.Timestamped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchLog extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "search_keywords", joinColumns = @JoinColumn(name = "search_log_id"))
    @Column(name = "keyword")
    private List<String> keywords;

    private String mainKeyword;

    private String brand;

    @Builder
    public SearchLog(List<String> keywords, String mainKeyword, String brand) {
        this.keywords = keywords;
        this.mainKeyword = mainKeyword;
        this.brand = brand;
    }
}
