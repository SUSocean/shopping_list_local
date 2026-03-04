package com.SUSocean.Shopping_List.controllers;

import com.SUSocean.Shopping_List.domain.dto.RequestUserDto;
import com.SUSocean.Shopping_List.domain.dto.SimpleListDto;
import com.SUSocean.Shopping_List.domain.dto.UserDto;
import com.SUSocean.Shopping_List.domain.entities.ListEntity;
import com.SUSocean.Shopping_List.domain.entities.UserEntity;
import com.SUSocean.Shopping_List.mappers.impl.SimpleListMapper;
import com.SUSocean.Shopping_List.mappers.impl.UserDtoMapper;
import com.SUSocean.Shopping_List.services.ListService;
import com.SUSocean.Shopping_List.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;


@RestController
public class UserController {

    private UserService userService;
    private ListService listService;
    private UserDtoMapper userDtoMapper;
    private SimpleListMapper simpleListMapper;

    public UserController(UserService userService,
                          UserDtoMapper userDtoMapper,
                          SimpleListMapper simpleListMapper,
                          ListService listService) {
        this.userService = userService;
        this.listService = listService;
        this.userDtoMapper = userDtoMapper;
        this.simpleListMapper = simpleListMapper;
    }

    @PostMapping(path = "/api/users")
    public ResponseEntity<UserDto> createUser(
            HttpSession httpSession,
            @RequestBody RequestUserDto user
    ){

        UserEntity savedUserEntity = userService.saveUser(user);
        httpSession.setAttribute("userId", savedUserEntity.getId());
        return new ResponseEntity<>(userDtoMapper.mapToUserDto(savedUserEntity), HttpStatus.CREATED);
    }

    @GetMapping(path = "/api/users/me")
    public ResponseEntity<UserDto> getUser(
            HttpSession httpSession
    ){
        Long userId = (Long) httpSession.getAttribute("userId");

        UserEntity user = userService.getUser(userId);

        return new ResponseEntity<>(userDtoMapper.mapToUserDto(user), HttpStatus.OK);
    }

    @DeleteMapping(path = "/api/users")
    public ResponseEntity<Void> deleteUser(HttpSession httpSession){
        Long userId = (Long) httpSession.getAttribute("userId");

        userService.deleteUser(userId);

        httpSession.invalidate();

        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/api/users/lists")
    public ResponseEntity<SimpleListDto> createList(
            HttpSession httpSession,
            @RequestBody SimpleListDto list
    ){
        Long userId = (Long) httpSession.getAttribute("userId");

        ListEntity savedListEntity = listService.createList(list, userId);
        return new ResponseEntity<>(simpleListMapper.mapToSimpleListDto(savedListEntity), HttpStatus.CREATED);
    }

    @PatchMapping(path = "/api/users/lists/remove/{list_id}")
    public ResponseEntity<SimpleListDto> removeList(
            HttpSession httpSession,
            @PathVariable("list_id") UUID list_id
    ){
        Long userId = (Long) httpSession.getAttribute("userId");

        ListEntity removedListEntity = userService.removeList(userId, list_id);

        return new ResponseEntity<>(simpleListMapper.mapToSimpleListDto(removedListEntity), HttpStatus.OK);
    }
}
