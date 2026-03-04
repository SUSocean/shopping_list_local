package com.SUSocean.Shopping_List.services.impl;

import com.SUSocean.Shopping_List.domain.dto.ItemDto;
import com.SUSocean.Shopping_List.domain.dto.RequestItemDto;
import com.SUSocean.Shopping_List.domain.entities.ItemEntity;
import com.SUSocean.Shopping_List.domain.entities.ListEntity;
import com.SUSocean.Shopping_List.domain.entities.UserEntity;
import com.SUSocean.Shopping_List.exception.BadRequestException;
import com.SUSocean.Shopping_List.exception.ForbiddenException;
import com.SUSocean.Shopping_List.exception.NotFoundException;
import com.SUSocean.Shopping_List.repositories.ItemRepository;
import com.SUSocean.Shopping_List.repositories.ListRepository;
import com.SUSocean.Shopping_List.repositories.UserRepository;
import com.SUSocean.Shopping_List.services.ItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ItemServiceImpl implements ItemService {

    private ListRepository listRepository;
    private UserRepository userRepository;
    private ItemRepository itemRepository;

    public ItemServiceImpl(
            ListRepository listRepository,
            UserRepository userRepository,
            ItemRepository itemRepository
    ) {
        this.listRepository = listRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    @Transactional
    public ItemEntity createItem(Long userId, UUID listId, RequestItemDto requestItemDto) {

        if(userId == null || listId == null){
            throw new BadRequestException("wrong user or list id");
        }

        if(requestItemDto.getName().length() < 3){
            throw new BadRequestException("list name must be at least 3 characters");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ListEntity listEntity = listRepository.findById(listId)
                .orElseThrow(() -> new NotFoundException("List not found"));

        if (!listEntity.getUsers().contains(user)){
            throw new ForbiddenException("Not a list member");
        }

        ItemEntity itemEntity = ItemEntity.builder()
                .name(requestItemDto.getName())
                .isActive(true)
                .list(listEntity)
                .position(listEntity.getItems().size())
                .build();

        listEntity.getItems().add(itemEntity);

        return itemRepository.save(itemEntity);
    }

    @Override
    @Transactional
    public ItemEntity removeItem(Long userId, UUID listId, UUID itemId) {

        if (userId == null || listId == null | itemId == null){
            throw new BadRequestException("user, list or item id is wrong");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ListEntity listEntity = listRepository.findById(listId)
                .orElseThrow(() -> new NotFoundException("List not found"));

        if (!listEntity.getUsers().contains(user)){
            throw new ForbiddenException("Not a list member");
        }

        ItemEntity itemEntity = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("No item found"));


        if (!itemEntity.getList().getId().equals(listId)) {
            throw new ForbiddenException("Item does not belong to this list");
        }

        int removedPosition = itemEntity.getPosition();

        listEntity.getItems().remove(itemEntity);

        for(ItemEntity item : listEntity.getItems()){
            if(item.getPosition() > removedPosition){
                item.setPosition(item.getPosition() - 1);
            }
        }

        return itemEntity;
    }

    @Override
    @Transactional
    public ItemEntity editItem(Long userId, UUID listId, UUID item_id, ItemDto item) {
        if (userId == null || listId == null){
            throw new BadRequestException("User or list id is wrong");
        }

        if (item.getName().length() < 3){
            throw new BadRequestException("List name must be at leat 3 characters");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ListEntity listEntity = listRepository.findById(listId)
                .orElseThrow(() -> new NotFoundException("List not found"));

        if (!listEntity.getUsers().contains(user)){
            throw new ForbiddenException("Not a list member");
        }

        ItemEntity itemEntity = itemRepository.findById(item_id)
                .orElseThrow(() -> new NotFoundException("No item found"));


        if (!itemEntity.getList().getId().equals(listId)) {
            throw new ForbiddenException("Item does not belong to this list");
        }

        itemEntity.setActive(item.getActive());
        itemEntity.setName(item.getName());

        return itemEntity;
    }
}
