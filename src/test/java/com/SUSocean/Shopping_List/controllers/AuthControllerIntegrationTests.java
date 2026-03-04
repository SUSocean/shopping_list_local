package com.SUSocean.Shopping_List.controllers;

import com.SUSocean.Shopping_List.TestDataUtil;
import com.SUSocean.Shopping_List.domain.dto.RequestUserDto;
import com.SUSocean.Shopping_List.domain.entities.UserEntity;
import com.SUSocean.Shopping_List.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class AuthControllerIntegrationTests {
    private UserService userService;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Autowired
    public AuthControllerIntegrationTests(UserService userService, MockMvc mockMvc, ObjectMapper objectMapper) {
        this.userService = userService;
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    public void testThatAuthLoginReturns200OkWhenCredentialsAreCorrect() throws Exception {
        RequestUserDto testRequestUserDto = TestDataUtil.createRequestUserDtoA();
        userService.saveUser(testRequestUserDto);

        String testRequestUserDtoJson = objectMapper.writeValueAsString(testRequestUserDto);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testRequestUserDtoJson)
        ).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testThatAuthLoginReturn400BadRequestWhenCredentialAreEmpty() throws Exception{

        RequestUserDto testRequestUserDto = TestDataUtil.createRequestUserDtoA();
        testRequestUserDto.setUsername("");
        String testRequestUserDtoJson = objectMapper.writeValueAsString(testRequestUserDto);


        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testRequestUserDtoJson)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest()
        ).andExpect(MockMvcResultMatchers.jsonPath("$.message")
                .value("Blank username or password"));
    }


    @Test
    public void testThatAuthCheckReturn401WhenHttpsSessionIsNotSet() throws Exception{
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/auth/check")
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void testThatAuthCheckReturn200WhenHttpsSessionIsSet() throws Exception{
        RequestUserDto testRequestUserDto = TestDataUtil.createRequestUserDtoA();
        UserEntity savedUserEntity = userService.saveUser(testRequestUserDto);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("userId", savedUserEntity.getId());


        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/auth/check")
                        .sessionAttrs(sessionAttrs)
        ).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testThatAuthLoginReturns401UnauthorizedWhenCredentialAreIncorrect() throws Exception {
        RequestUserDto testRequestUserDtoA = TestDataUtil.createRequestUserDtoA();
        RequestUserDto testRequestUserDtoB = TestDataUtil.createRequestUserDtoB();
        userService.saveUser(testRequestUserDtoA);
        String testRequestUserDtoBJson = objectMapper.writeValueAsString(testRequestUserDtoB);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testRequestUserDtoBJson)
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized()
        ).andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    public void testThatAuthLogoutReturns200OkAndInvalidatesHttpSession() throws Exception {
        RequestUserDto testRequestUserDto = TestDataUtil.createRequestUserDtoA();
        UserEntity savedUser = userService.saveUser(testRequestUserDto);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("userId", savedUser.getId());

        mockMvc.perform(
                MockMvcRequestBuilders.post("/auth/logout")
                        .sessionAttrs(sessionAttrs)
        ).andExpect(MockMvcResultMatchers.status().isOk()
    ).andExpect(MockMvcResultMatchers.request().sessionAttributeDoesNotExist("userId"));
    }
}
