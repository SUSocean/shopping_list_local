package com.SUSocean.Shopping_List.controllers;

import com.SUSocean.Shopping_List.domain.dto.RequestUserDto;
import com.SUSocean.Shopping_List.domain.dto.UserDto;
import com.SUSocean.Shopping_List.domain.entities.UserEntity;
import com.SUSocean.Shopping_List.mappers.impl.UserDtoMapper;
import com.SUSocean.Shopping_List.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthContoller {

    UserService userService;
    UserDtoMapper userDtoMapper;

    public AuthContoller(UserService userService, UserDtoMapper userDtoMapper) {
        this.userService = userService;
        this.userDtoMapper = userDtoMapper;
    }

    @PostMapping(path = "/api/auth/login")
    public ResponseEntity<UserDto> login(
            @RequestBody RequestUserDto requestUserDto,
            HttpSession httpSession
    ){
        UserEntity foundUserEntity = userService.verifyUser(
                requestUserDto.getUsername(),
                requestUserDto.getPassword()
        );

        httpSession.setAttribute("userId", foundUserEntity.getId());

        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/api/auth/logout")
    public ResponseEntity<Void> logout(HttpSession httpSession){

        httpSession.invalidate();

        return ResponseEntity.ok().build();

    }

    @GetMapping("/api/auth/check")
    public ResponseEntity<Boolean> checkSession(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }

        return ResponseEntity.ok(true);
    }
}
