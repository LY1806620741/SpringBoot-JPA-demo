package com.example.demo.controller.event;

import com.example.demo.DemoApplication;
import com.example.demo.repository.query.QueryMessageRepository;
import com.example.demo.repository.query.QueryUserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class)
@Transactional//事务回滚，单个测试间数据不影响
@ActiveProfiles("test")//测试使用H2内存数据库，调用application-test.yml
public class MessageEventApiTest {
    private MockMvc restMockMvc;
    @Autowired private MessageEventApi messageEventApi;
    @Autowired private AccountEventApi accountEventApi;
    @Autowired private QueryUserRepository queryUserRepository;
    @Autowired private QueryMessageRepository queryMessageRepository;
    @Before
    public void setup(){
        //初始化mockmvc，并使用page解码
        MockitoAnnotations.initMocks(this);
        this.restMockMvc = MockMvcBuilders.standaloneSetup(messageEventApi)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver()).build();
    }
    @Test
    public void say() throws Exception {
        restMockMvc.perform(post("/message/say").param("id","1").param("data","test")).andExpect(status().isBadRequest());
        //通过调用方法进行新建用户
        assertThat(accountEventApi.createUser("user1","123",null).getStatusCode(),is(HttpStatus.valueOf(200)));
        //得到用户id
        Long id =queryUserRepository.findOneByName("user1").get().getId();
        restMockMvc.perform(post("/message/say").param("id",id.toString()).param("data","test")).andExpect(status().isOk());
    }

    @Test
    public void delete() throws Exception {
        restMockMvc.perform(get("/message/delete/1")).andExpect(status().isBadRequest());
        say();//调用say创建
        Long message_id=queryMessageRepository.findOne(Specification.where(null)).get().getId();
        restMockMvc.perform(get("/message/delete/"+message_id)).andExpect(status().isOk());
    }
}