package com.SUSocean.Shopping_List.controllers;

import com.SUSocean.Shopping_List.TestDataUtil;
import com.SUSocean.Shopping_List.domain.dto.*;
import com.SUSocean.Shopping_List.domain.entities.ItemEntity;
import com.SUSocean.Shopping_List.domain.entities.ListEntity;
import com.SUSocean.Shopping_List.domain.entities.UserEntity;
import com.SUSocean.Shopping_List.mappers.impl.SimpleUserMapper;
import com.SUSocean.Shopping_List.services.ItemService;
import com.SUSocean.Shopping_List.services.ListService;
import com.SUSocean.Shopping_List.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
//import com.fasterxml.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class ListControllerIntegrationTests {

    private UserService userService;

    private ListService listService;

    private ItemService itemService;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private SimpleUserMapper simpleUserMapper;

    @Autowired
    public ListControllerIntegrationTests(
            UserService userService,
            ItemService itemService,
            ListService listService,
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            SimpleUserMapper simpleUserMapper
    ) {
        this.userService = userService;
        this.listService = listService;
        this.itemService = itemService;
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.simpleUserMapper = simpleUserMapper;
    }

    @Test
    public void testThatGetListReturnsCorrectOpenedListDtoWhenListExist() throws Exception{
        SimpleListDto simpleListDto = TestDataUtil.createSimpleListDtoA();
        RequestUserDto requestUserDto = TestDataUtil.createRequestUserDtoA();

        UserEntity savedUserEntity = userService.saveUser(requestUserDto);
        SimpleUserDto simpleUserDto = simpleUserMapper.mapToSimpleUserDto(savedUserEntity);
        ListEntity savedListEntity =  listService.createList(simpleListDto, savedUserEntity.getId());

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("userId", savedUserEntity.getId());

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/lists/" + savedListEntity.getId())
                        .sessionAttrs(sessionAttrs)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").value(savedListEntity.getId().toString())

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.name").value(savedListEntity.getName())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.creator").value(simpleUserDto)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.users[0]").value(simpleUserDto)
        );
    }

    @Test
    public void testThatGetListReturnsForbiddenWhenUserNotLoggedIn() throws Exception{
        SimpleListDto simpleListDto = TestDataUtil.createSimpleListDtoA();
        RequestUserDto requestUserDto = TestDataUtil.createRequestUserDtoA();

        UserEntity savedUserEntity = userService.saveUser(requestUserDto);
        ListEntity savedListEntity =  listService.createList(simpleListDto, savedUserEntity.getId());

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/lists/" + savedListEntity.getId())
        ).andExpect(
                MockMvcResultMatchers.status().isForbidden()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.message").value("Not logged in")
        );
    }

    @Test
    public void testThatGetListReturnForbiddenWhenUserIsNotAListMember() throws Exception{
        SimpleListDto simpleListDto = TestDataUtil.createSimpleListDtoA();

        RequestUserDto requestUserDtoA = TestDataUtil.createRequestUserDtoA();
        RequestUserDto requestUserDtoB = TestDataUtil.createRequestUserDtoB();

        UserEntity savedUserEntityA = userService.saveUser(requestUserDtoA);
        UserEntity savedUserEntityB = userService.saveUser(requestUserDtoB);

        ListEntity savedListEntity =  listService.createList(simpleListDto, savedUserEntityA.getId());

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("userId", savedUserEntityB.getId());

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/lists/" + savedListEntity.getId())
                        .sessionAttrs(sessionAttrs)
        ).andExpect(
                MockMvcResultMatchers.status().isForbidden()

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.message").value("Not a member of a list")
        );
    }

    @Test
    public void testThatAddUserReturnsCorrectOpenedListDtoWithAddedUser() throws Exception{

        RequestUserDto requestUserDtoA = TestDataUtil.createRequestUserDtoA();
        RequestUserDto requestUserDtoB = TestDataUtil.createRequestUserDtoB();

        UserEntity savedUserEntityA = userService.saveUser(requestUserDtoA);
        UserEntity savedUserEntityB = userService.saveUser(requestUserDtoB);

        SimpleListDto simpleListDto = TestDataUtil.createSimpleListDtoA();
        ListEntity savedListEntity =  listService.createList(simpleListDto, savedUserEntityA.getId());

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("userId", savedUserEntityA.getId());

        SimpleUserDto simpleUserDtoB = simpleUserMapper.mapToSimpleUserDto(savedUserEntityB);

        String SimpleUserDtoBJson = objectMapper.writeValueAsString(simpleUserDtoB);

        mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/lists/" + savedListEntity.getId() + "/users/add")
                        .sessionAttrs(sessionAttrs)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SimpleUserDtoBJson)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.users[1]").value(simpleUserDtoB)
        );
    }

    @Test
    public void testThatRemoveUserReturnsCorrectOpenedListDtoWithoutAddedUser() throws Exception{

        RequestUserDto requestUserDtoA = TestDataUtil.createRequestUserDtoA();
        RequestUserDto requestUserDtoB = TestDataUtil.createRequestUserDtoB();

        UserEntity savedUserEntityA = userService.saveUser(requestUserDtoA);
        UserEntity savedUserEntityB = userService.saveUser(requestUserDtoB);

        SimpleListDto simpleListDto = TestDataUtil.createSimpleListDtoA();
        ListEntity savedListEntity =  listService.createList(simpleListDto, savedUserEntityA.getId());

        SimpleUserDto simpleUserDtoB = simpleUserMapper.mapToSimpleUserDto(savedUserEntityB);

        String SimpleUserDtoBJson = objectMapper.writeValueAsString(simpleUserDtoB);

        listService.addUser(savedListEntity.getId(), savedUserEntityA.getId(), simpleUserDtoB);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("userId", savedUserEntityA.getId());

        mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/lists/" + savedListEntity.getId() + "/users/remove")
                        .sessionAttrs(sessionAttrs)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SimpleUserDtoBJson)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.users[1]").doesNotExist()
        );
    }

    @Test
    public void testThatCreateItemReturnsCreatedItem() throws Exception{
        SimpleListDto simpleListDto = TestDataUtil.createSimpleListDtoA();
        RequestUserDto requestUserDto = TestDataUtil.createRequestUserDtoA();

        UserEntity savedUserEntity = userService.saveUser(requestUserDto);
        ListEntity savedListEntity =  listService.createList(simpleListDto, savedUserEntity.getId());

        RequestItemDto requestItemDtoA = TestDataUtil.createRequestItemDtoA();
        String requestItemDtoAJson = objectMapper.writeValueAsString(requestItemDtoA);
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("userId", savedUserEntity.getId());

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/lists/" + savedListEntity.getId() + "/item/add")
                        .sessionAttrs(sessionAttrs)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestItemDtoAJson)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").exists()

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.name").value(requestItemDtoA.getName())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.active").value(true)
        );
    }

    @Test
    public void testThatRemoveItemReturnsRemovedItem() throws Exception{
        SimpleListDto simpleListDto = TestDataUtil.createSimpleListDtoA();
        RequestUserDto requestUserDto = TestDataUtil.createRequestUserDtoA();

        UserEntity savedUserEntity = userService.saveUser(requestUserDto);
        ListEntity savedListEntity =  listService.createList(simpleListDto, savedUserEntity.getId());

        RequestItemDto requestItemDtoA = TestDataUtil.createRequestItemDtoA();

        ItemEntity savedItem = itemService
                .createItem(savedUserEntity.getId(),savedListEntity.getId(), requestItemDtoA);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("userId", savedUserEntity.getId());

        mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/lists/" + savedListEntity.getId() + "/item/"+ savedItem.getId() +"/remove")
                        .sessionAttrs(sessionAttrs)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").value(savedItem.getId().toString())

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.name").value(savedItem.getName())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.active").value(true)
        );
    }

    @Test
    public void testThatEditItemReturnsEditedItem() throws Exception{
        SimpleListDto simpleListDto = TestDataUtil.createSimpleListDtoA();
        RequestUserDto requestUserDto = TestDataUtil.createRequestUserDtoA();

        UserEntity savedUserEntity = userService.saveUser(requestUserDto);
        ListEntity savedListEntity =  listService.createList(simpleListDto, savedUserEntity.getId());

        RequestItemDto requestItemDtoA = TestDataUtil.createRequestItemDtoA();

        ItemEntity savedItem = itemService.createItem(savedUserEntity.getId(), savedListEntity.getId(), requestItemDtoA);

        ItemDto itemDtoB = TestDataUtil.createInactiveItemDtoB();
        String itemBDtoJson = objectMapper.writeValueAsString(itemDtoB);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("userId", savedUserEntity.getId());

        mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/lists/" + savedListEntity.getId() + "/item/" + savedItem.getId() + "/edit")
                        .sessionAttrs(sessionAttrs)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemBDtoJson)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").value(savedItem.getId().toString())

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.name").value(itemDtoB.getName())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.active").value(itemDtoB.getActive())
        );
    }

    @Test
    public void testThatReorderListReturnsEditedList() throws Exception{
        SimpleListDto simpleListDto = TestDataUtil.createSimpleListDtoA();
        RequestUserDto requestUserDto = TestDataUtil.createRequestUserDtoA();

        UserEntity savedUserEntity = userService.saveUser(requestUserDto);
        ListEntity savedListEntity =  listService.createList(simpleListDto, savedUserEntity.getId());

        RequestItemDto requestItemDtoA = TestDataUtil.createRequestItemDtoA();
        RequestItemDto requestItemDtoB = TestDataUtil.createRequestItemDtoB();

        ItemEntity savedItemA = itemService.createItem(savedUserEntity.getId(), savedListEntity.getId(), requestItemDtoA);
        ItemEntity savedItemB = itemService.createItem(savedUserEntity.getId(), savedListEntity.getId(), requestItemDtoB);

        List<UUID> newOrder = new ArrayList<>();
        newOrder.add(savedItemB.getId());
        newOrder.add(savedItemA.getId());

        RequestReorderListDto requestReorderListDto = RequestReorderListDto.builder()
                .itemsOrder(newOrder)
                .build();

        String requestListDtoJson = objectMapper.writeValueAsString(requestReorderListDto);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("userId", savedUserEntity.getId());
        mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/lists/" + savedListEntity.getId() + "/reorder")
                        .sessionAttrs(sessionAttrs)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestListDtoJson)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[0].id").value(savedItemB.getId().toString())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[0].position").value(0)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[1].id").value(savedItemA.getId().toString())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[1].position").value(1)
        );
    }

    @Test
    public void testThatRenameListReturnsRenamedList() throws Exception {
        SimpleListDto simpleListDto = TestDataUtil.createSimpleListDtoA();
        RequestUserDto requestUserDto = TestDataUtil.createRequestUserDtoA();

        UserEntity savedUserEntity = userService.saveUser(requestUserDto);
        ListEntity savedListEntity =  listService.createList(simpleListDto, savedUserEntity.getId());

        simpleListDto.setName("new name");

        String simpleListDtoJson = objectMapper.writeValueAsString(simpleListDto);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("userId", savedUserEntity.getId());

        mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/lists/" + savedListEntity.getId() + "/rename")
                        .sessionAttrs(sessionAttrs)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(simpleListDtoJson)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.name").value("new name")
        );
    }
}
