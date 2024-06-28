package com.springboot.homework;

import com.jayway.jsonpath.JsonPath;
import com.springboot.member.dto.MemberDto;
import com.google.gson.Gson;
import com.springboot.member.dto.MemberPatchDto;
import com.springboot.member.entity.Member;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class MemberControllerHomeworkTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Gson gson;

    @Test
    void postMemberTest() throws Exception {
        // given
        MemberDto.Post post = new MemberDto.Post("hgd@gmail.com",
                "홍길동",
                "010-1234-5678");
        String content = gson.toJson(post);


        // when
        ResultActions actions =
                mockMvc.perform(
                        post("/v11/members")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                );

        // then
        actions
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", is(startsWith("/v11/members/"))));
    }

    @Test
    void patchMemberTest() throws Exception {
        // TODO MemberController의 patchMember() 핸들러 메서드를 테스트하는 테스트 케이스를 여기에 작성하세요.
        // given
        MemberDto.Post post = new MemberDto.Post("hgd@gmail.com",
                "홍길동",
                "010-1234-5678");
        String content = gson.toJson(post);


        // when
        ResultActions actions =
                mockMvc.perform(
                        post("/v11/members/")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                );

        long memberId;
        String location = actions.andReturn().getResponse().getHeader("Location"); // "/v11/members/1"
        memberId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        MemberDto.Patch patch = MemberDto.Patch.builder()
                .phone("010-1234-1234")
                .build();

        content = gson.toJson(patch);

        ResultActions patchActions =
                mockMvc.perform(
                        patch("/v11/members/" + memberId)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                );

        mockMvc.perform(
                        get("/v11/members/" + memberId)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phone").value(patch.getPhone()));

    }

    @Test
    void getMemberTest() throws Exception {
        // given: MemberController의 getMember()를 테스트하기 위해서 postMember()를 이용해 테스트 데이터를 생성 후, DB에 저장
        MemberDto.Post post = new MemberDto.Post("hgd@gmail.com","홍길동","010-1111-1111");
        String postContent = gson.toJson(post);

        ResultActions postActions =
                mockMvc.perform(
                        post("/v11/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(postContent)
                );
        long memberId;
        String location = postActions.andReturn().getResponse().getHeader("Location"); // "/v11/members/1"
        memberId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        // when / then
        mockMvc.perform(
                        get("/v11/members/" + memberId)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(post.getEmail()))
                .andExpect(jsonPath("$.data.name").value(post.getName()))
                .andExpect(jsonPath("$.data.phone").value(post.getPhone()));
    }

    @Test
    void getMembersTest() throws Exception {
        // TODO MemberController의 getMembers() 핸들러 메서드를 테스트하는 테스트 케이스를 여기에 작성하세요.
        // given: MemberController의 getMember()를 테스트하기 위해서 postMember()를 이용해 테스트 데이터를 생성 후, DB에 저장

        int page = 1;
        int size = new Random().nextInt(10);

        List<MemberDto.Post> postList = new ArrayList<>();
        for (int i =0 ;i < size; i++){
            MemberDto.Post post = new MemberDto.Post("hgd" + i + "@gmail.com","홍길동" + i ,"010-1111-111" + i);
            String postContent = gson.toJson(post);

            ResultActions postActions =
                    mockMvc.perform(
                            post("/v11/members")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content(postContent)
                    );

            postList.add(post);
        }

        ResultActions getAction = mockMvc.perform(
                get("/v11/members")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON)
        );

        MvcResult result =  getAction
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(content);
        JSONArray array = (JSONArray) parser.parse(obj.get("data").toString());
        JSONObject pageInfoObj = (JSONObject) ((JSONObject) parser.parse(content)).get("pageInfo");
        assertEquals(pageInfoObj.get("size"), size);
        assertEquals(pageInfoObj.get("page"), page);

        for (int i =0 ; i < array.size(); i++){
            JSONObject member = (JSONObject) parser.parse(array.get(i).toString());
            Optional<MemberDto.Post> memberDto = postList.stream()
                    .filter(x -> x.getEmail().equals(member.get("email")))
                    .findFirst();
            assertEquals(member.get("name"), memberDto.get().getName());
            assertEquals(member.get("phone"), memberDto.get().getPhone());
            assertEquals(member.get("email"), memberDto.get().getEmail());
        }

        //갯수까지만 검증하는 경우도 있음
        List list = JsonPath.parse(result.getResponse().getContentAsString()).read("$.data");
        assertEquals(list.size(), size);
    }

    @Test
    void deleteMemberTest() throws Exception {
        // TODO MemberController의 deleteMember() 핸들러 메서드를 테스트하는 테스트 케이스를 여기에 작성하세요.
        MemberDto.Post post = new MemberDto.Post("hgd@gmail.com","홍길동","010-1111-1111");
        String postContent = gson.toJson(post);

        ResultActions postActions =
                mockMvc.perform(
                        post("/v11/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(postContent)
                );
        long memberId;
        String location = postActions.andReturn().getResponse().getHeader("Location"); // "/v11/members/1"
        memberId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        // when / then
        mockMvc.perform(
                        delete("/v11/members/" + memberId)
                )
                .andExpect(status().isNoContent());

        // 안 해도 됨!!
        mockMvc.perform(
                        get("/v11/members/" + memberId)
                )
                .andExpect(status().isNotFound());

    }
}
