package com.bms.userservice.service;

import com.bms.userservice.dto.UserDto;
import com.bms.userservice.dto.UserUpdateRequest;
import com.bms.userservice.entity.User;
import com.bms.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(String username) {
        return getByUsername(username);
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + id));
        return toDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public UserDto updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + id));

        String email = request.getEmail().trim().toLowerCase();
        String fullName = request.getFullName().trim();
        String phone = request.getPhone().trim();
        String address = request.getAddress().trim();

        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (userRepository.existsByPhoneAndIdNot(phone, id)) {
            throw new IllegalArgumentException("Phone number already exists");
        }

        user.setEmail(email);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setAddress(address);

        return toDto(userRepository.save(user));
    }

    private UserDto getByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return toDto(user);
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .build();
    }
}
