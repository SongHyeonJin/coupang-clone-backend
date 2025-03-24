package com.example.coupangclone.user.entity;

import com.example.coupangclone.global.common.Timestamped;
import com.example.coupangclone.user.enums.UserRoleEnum;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "USERS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String tel;

    private Boolean isAdult = false;

    private String residentNumber;

    private String gender;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRoleEnum role;

    private Boolean isDeleted = false;

    @Builder
    public User(String email, String password, String name, String tel, String gender, UserRoleEnum role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.tel = tel;
        this.gender = gender;
        this.role = role;
    }

}

