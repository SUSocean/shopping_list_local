package com.SUSocean.Shopping_List.controllers;

import com.SUSocean.Shopping_List.domain.dto.*;
import com.SUSocean.Shopping_List.domain.entities.ItemEntity;
import com.SUSocean.Shopping_List.domain.entities.ListEntity;
import com.SUSocean.Shopping_List.mappers.impl.ItemMapper;
import com.SUSocean.Shopping_List.mappers.impl.OpenedListMapper;
import com.SUSocean.Shopping_List.services.ItemService;
import com.SUSocean.Shopping_List.services.ListService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class ListController {

    private ListService listService;
    private ItemService itemService;
    private OpenedListMapper openedListMapper;
    private ItemMapper itemMapper;

    public ListController(
            ListService listService,
            ItemService itemService,
            OpenedListMapper openedListMapper,
            ItemMapper itemMapper
    ) {
        this.listService = listService;
        this.itemService = itemService;
        this.openedListMapper = openedListMapper;
        this.itemMapper = itemMapper;
    }

    @GetMapping(path = "/api/lists/{list_id}")
    public ResponseEntity<OpenedListDto> getList(
            HttpSession httpSession,
            @PathVariable("list_id") UUID list_id
    ){
        Long userId = (Long) httpSession.getAttribute("userId");

        ListEntity listEntity = listService.findById(list_id, userId);

        return new ResponseEntity<>(openedListMapper.mapToOpenedListDto(listEntity), HttpStatus.OK);
    }

    @PatchMapping(path = "/api/lists/{list_id}/users/add")
    public ResponseEntity<OpenedListDto> addUser(
            HttpSession httpSession,
            @PathVariable("list_id") UUID list_id,
            @RequestBody SimpleUserDto user
    ){
        Long creatorId = (Long) httpSession.getAttribute("userId");

        ListEntity listEntity = listService.addUser(list_id, creatorId, user);

        return new ResponseEntity<>(openedListMapper.mapToOpenedListDto(listEntity), HttpStatus.OK);
    }

    @PatchMapping(path = "/api/lists/{list_id}/users/remove")
    public ResponseEntity<OpenedListDto> removeUser(
            HttpSession httpSession,
            @PathVariable("list_id") UUID list_id,
            @RequestBody SimpleUserDto user
    ){
        Long creatorId = (Long) httpSession.getAttribute("userId");

        ListEntity listEntity = listService.removeUser(list_id, creatorId, user);

        return new ResponseEntity<>(openedListMapper.mapToOpenedListDto(listEntity), HttpStatus.OK);
    }

    @PostMapping(path = "/api/lists/{list_id}/item/add")
    public ResponseEntity<ItemDto> createItem(
            HttpSession httpSession,
            @PathVariable("list_id") UUID list_id,
            @RequestBody RequestItemDto requestItemDto
    ){
        Long userId = (Long) httpSession.getAttribute("userId");

        ItemEntity itemEntity = itemService.createItem(userId, list_id, requestItemDto);

        return new ResponseEntity<>(itemMapper.mapToItemDto(itemEntity), HttpStatus.OK);
    }

    @PatchMapping(path = "/api/lists/{list_id}/item/{item_id}/remove")
    public ResponseEntity<ItemDto> removeItem(
            HttpSession httpSession,
            @PathVariable("list_id") UUID list_id,
            @PathVariable("item_id") UUID item_id
    ){
        Long userId = (Long) httpSession.getAttribute("userId");

        ItemEntity itemEntity = itemService.removeItem(userId, list_id, item_id);

        return new ResponseEntity<>(itemMapper.mapToItemDto(itemEntity), HttpStatus.OK);
    }

    @PatchMapping(path = "/api/lists/{list_id}/item/{item_id}/edit")
    public ResponseEntity<ItemDto> editItem(
            HttpSession httpSession,
            @PathVariable("list_id") UUID list_id,
            @PathVariable("item_id") UUID item_id,
            @RequestBody ItemDto item
    ){
        Long userId = (Long) httpSession.getAttribute("userId");

        ItemEntity itemEntity = itemService.editItem(userId, list_id, item_id, item);

        return new ResponseEntity<>(itemMapper.mapToItemDto(itemEntity), HttpStatus.OK);
    }

    @PatchMapping(path = "/api/lists/{list_id}/reorder")
    public ResponseEntity<List<ItemDto>> reorderList(
            HttpSession httpSession,
            @PathVariable("list_id") UUID list_id,
            @RequestBody RequestReorderListDto list
    ){
        Long userId = (Long) httpSession.getAttribute("userId");

        List<ItemDto> itemList = listService.reorderList(userId, list_id, list);

        return new ResponseEntity<>(itemList, HttpStatus.OK);
    }

    @PatchMapping(path = "/api/lists/{list_id}/rename")
    public ResponseEntity<SimpleListDto> renameList(
            HttpSession httpSession,
            @PathVariable("list_id") UUID list_id,
            @RequestBody RequestRenameListDto requestRenameListDto
    ){
        Long userId = (Long) httpSession.getAttribute("userId");

        SimpleListDto list = listService.renameList(userId, list_id, requestRenameListDto.getName());

        return new ResponseEntity<>(list, HttpStatus.OK);
    }
}
