package com.example.coupangclone.entity.item;

import com.example.coupangclone.enums.ItemTypeEnum;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ItemTypeEnum type;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Builder
    public Category(String name, ItemTypeEnum type, Category parent) {
        this.name = name;
        this.type = type;
        this.parent = parent;
    }
}
