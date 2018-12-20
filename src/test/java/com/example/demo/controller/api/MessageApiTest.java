package com.example.demo.controller.api;

import com.example.demo.DemoApplication;
import com.example.demo.domain.Message;
import com.example.demo.domain.User;
import com.example.demo.domain.enumeration.TopRankTime;
import com.example.demo.repository.query.QueryMessageRepository;
import com.example.demo.repository.query.QueryUserRepository;
import com.example.demo.tools.EntityCreate;
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
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class)
@ActiveProfiles("test")//测试使用H2内存数据库，调用application-test.yml
public class MessageApiTest {

    private MockMvc restMockMvc;
    @Autowired private MessageApi messageApi;
    @Autowired private QueryUserRepository queryUserRepository;
    @Autowired private QueryMessageRepository queryMessageRepository;

    @Before
    public void setup(){
        //初始化mockmvc，并使用page解码
        MockitoAnnotations.initMocks(this);
        this.restMockMvc = MockMvcBuilders.standaloneSetup(messageApi)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver()).build();
    }

    @Test
    @Transactional
    //测试messageapi中的list()方法
    public void list() throws Exception {
        //新建用户
        User user1= EntityCreate.getnewUser();
        user1.setName("test1");
        User user2= EntityCreate.getnewUser();
        user2.setName("test2");
        //创建到数据库
        queryUserRepository.saveAll(new ArrayList<User>(){{add(user1);add(user2);}});
        //新建说说
        EntityCreate.savenewMessage(queryMessageRepository,user1);
        EntityCreate.savenewMessage(queryMessageRepository,user2,"不知道说什么");
        //全查询
        restMockMvc.perform(get("/message/list")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        //按用户查询
        restMockMvc.perform(get("/message/list?userid="+user1.getId())).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        //按时间和用户查询
        restMockMvc.perform(get("/message/list?userid="+user1.getId()+"&saytime="+ TopRankTime.ThisWeek)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        //按模糊搜索查询
        restMockMvc.perform(get("/message/list?saydata=不知道")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));

    }

    @Test
    @Transactional
    public void count() throws Exception {
        //新建用户
        User user1=EntityCreate.savenewUser(queryUserRepository,"user1");
        User user2=EntityCreate.savenewUser(queryUserRepository,"user2");
        //新建说说
        EntityCreate.savenewMessage(queryMessageRepository,user1);
        EntityCreate.savenewMessage(queryMessageRepository,user1,"测试");
        EntityCreate.savenewMessage(queryMessageRepository,user2);
        restMockMvc.perform(get("/message/count")).andExpect(status().isOk()).andExpect(content().string("3"));
        restMockMvc.perform(get("/message/count?userid="+user1.getId())).andExpect(status().isOk()).andExpect(content().string("2"));
        restMockMvc.perform(get("/message/count?userid="+user1.getId())).andExpect(status().isOk()).andExpect(content().string("2"));
        restMockMvc.perform(get("/message/count?userid="+user1.getId()+"&saytime="+TopRankTime.ThisMonth)).andExpect(status().isOk()).andExpect(content().string("2"));
    }

    @Test
    @Transactional
    public void countbyuser() throws Exception {
        //新建用户
        User user1=EntityCreate.savenewUser(queryUserRepository,"user1");
        User user2=EntityCreate.savenewUser(queryUserRepository,"user2");
        //新建说说
        EntityCreate.savenewMessage(queryMessageRepository,user1);
        EntityCreate.savenewMessage(queryMessageRepository,user1,"测试");
        EntityCreate.savenewMessage(queryMessageRepository,user2);
        restMockMvc.perform(get("/message/countbyuser/"+user1.getId())).andExpect(status().isOk()).andExpect(content().string("2"));
    }

    @Test
    @Transactional
    public void countbycursor() throws Exception {
        //新建用户
        User user1=EntityCreate.savenewUser(queryUserRepository,"user1");
        User user2=EntityCreate.savenewUser(queryUserRepository,"user2");
        //新建说说
        EntityCreate.savenewMessage(queryMessageRepository,user1);
        //特殊30天前发的说说
        EntityCreate.savenewMessage(queryMessageRepository,user2);
        Message message=EntityCreate.getnewMessage(user1,"30天前发的");
        message.setTime(Instant.now().minus(Duration.ofDays(30)));
        queryMessageRepository.save(message);
        //左标未来5秒 期望查出0
        restMockMvc.perform(get("/message/countbycursor").param("left", Instant.now().plusSeconds(50L).toString())).andExpect(status().isOk()).andExpect(content().string("0"));
        //右标现在 期望查出3
        restMockMvc.perform(get("/message/countbycursor").param("right", Instant.now().toString())).andExpect(status().isOk()).andExpect(content().string("3"));
        //右标是一天前到现在 期望查出2
        restMockMvc.perform(get("/message/countbycursor").param("left",Instant.now().minus(Duration.ofDays(1)).toString()).param("right", Instant.now().toString())).andExpect(status().isOk()).andExpect(content().string("2"));
    }
}