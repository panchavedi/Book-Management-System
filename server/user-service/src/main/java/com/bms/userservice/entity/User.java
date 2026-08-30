package com.bms.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 30)
    private String role;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "full_name", length = 150)
    private String fullName;

    @Column(length = 500)
    private String address;
}