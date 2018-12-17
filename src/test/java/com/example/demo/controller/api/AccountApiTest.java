package com.example.demo.controller.api;

import com.example.demo.DemoApplication;
import com.example.demo.domain.User;
import com.example.demo.domain.enumeration.TopRankTime;
import com.example.demo.repository.query.QueryUserRepository;
import com.example.demo.tools.EntityCreate;
import net.sf.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class)
@Transactional
@ActiveProfiles("test")//测试使用H2内存数据库，调用application-test.yml
public class AccountApiTest {

    private MockMvc restMockMvc;
    @Autowired private AccountApi accountApi;
    @Autowired private QueryUserRepository userRepository;

    @Before
    public void setup(){
        //初始化mockmvc，并使用page解码
        MockitoAnnotations.initMocks(this);
        this.restMockMvc = MockMvcBuilders.standaloneSetup(accountApi)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver()).build();
    }

    @Test
    @Transactional
    public void listUser() throws Exception {
        //直接访问url /account/list,期待是200的返回码
        restMockMvc.perform(get("/account/list")).andExpect(status().isOk());
        //新建三个有所区别的用户做测试用例
        User user1= EntityCreate.getnewUser();
        userRepository.save(user1);
        User user2= EntityCreate.getnewUser();
        user2.setName("test2");
        user2.setArea("Liuzhou");
        userRepository.save(user2);
        User user3= EntityCreate.getnewUser();
        user3.setName("test3");
        user3.setArea("Liuzhou");
        user3.setCreatetime(Instant.now().minus(Duration.ofDays(7)));//上一周
        user3.setLogintime(Instant.now().minus(Duration.ofDays(7)));
        userRepository.save(user3);
        //进行不同参数的检验
        assertThat(JSONArray.fromObject(restMockMvc.perform(get("/account/list")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).size(),is(3));
        assertThat(JSONArray.fromObject(restMockMvc.perform(get("/account/list?name="+user1.getName())).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).size(),is(1));
        assertThat(JSONArray.fromObject(restMockMvc.perform(get("/account/list?area=Liuzhou")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).size(),is(2));
        assertThat(JSONArray.fromObject(restMockMvc.perform(get("/account/list?createtime="+ TopRankTime.ThisWeek)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).size(),is(2));
        assertThat(JSONArray.fromObject(restMockMvc.perform(get("/account/list?logintime="+ TopRankTime.ThisMonth)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).size(),isIn(new Integer[]{0,1}));//user1,user2未登录，如果当前时间是月初7天内得到0，不然是1
    }

    @Test
    @Transactional
    public void pageUser() throws Exception {
        //注释同上
        restMockMvc.perform(get("/account/list")).andExpect(status().isOk());
        User user1= EntityCreate.getnewUser();
        userRepository.save(user1);
        User user2= EntityCreate.getnewUser();
        user2.setName("test2");
        user2.setArea("Liuzhou");
        userRepository.save(user2);
        User user3= EntityCreate.getnewUser();
        user3.setName("test3");
        user3.setArea("Liuzhou");
        user3.setCreatetime(Instant.now().minus(Duration.ofDays(7)));//上一周
        user3.setLogintime(Instant.now().minus(Duration.ofDays(7)));
        userRepository.save(user3);
        restMockMvc.perform(get("/account/page")).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(3));
        restMockMvc.perform(get("/account/page?name="+user1.getName())).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        restMockMvc.perform(get("/account/page?area=Liuzhou")).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2));
        restMockMvc.perform(get("/account/page?createtime="+ TopRankTime.ThisWeek)).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2));
        restMockMvc.perform(get("/account/page?logintime="+ TopRankTime.ThisMonth)).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(isIn(new Integer[]{0,1})));//user1,user2未登录
    }

    @Test
    @Transactional
    public void gettop() throws Exception {
        User user1= EntityCreate.getnewUser();
        userRepository.save(user1);
        User user2= EntityCreate.getnewUser();
        user2.setName("test2");
        user2.setArea("Liuzhou");
        userRepository.save(user2);
        User user3= EntityCreate.getnewUser();
        user3.setName("test3");
        user3.setArea("Liuzhou");
        user3.setCreatetime(Instant.now().minus(Duration.ofDays(7)));//上一周
        user3.setLogintime(Instant.now().minus(Duration.ofDays(7)));
        userRepository.save(user3);
        restMockMvc.perform(get("/account/top")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));//user3
    }
}