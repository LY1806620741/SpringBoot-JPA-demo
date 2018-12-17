package com.example.demo.controller.event;

import com.example.demo.DemoApplication;
import com.example.demo.domain.User;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class)
@Transactional//事务回滚，单个测试间数据不影响
@ActiveProfiles("test")//测试使用H2内存数据库，调用application-test.yml
public class AccountEventApiTest {
    private MockMvc restMockMvc;
    @Autowired private AccountEventApi accountEventApi;
    @Autowired private QueryUserRepository queryUserRepository;

    @Before
    public void setup(){
        //初始化mockmvc，并使用page解码
        MockitoAnnotations.initMocks(this);
        this.restMockMvc = MockMvcBuilders.standaloneSetup(accountEventApi)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver()).build();
    }

    @Test
    public void createUser() throws Exception {
        //只输入了用户名，期待失败
        restMockMvc.perform(post("/account/create").param("username","123")).andExpect(status().isBadRequest());
        //使用params方法输入用户名密码，期待成功
        MultiValueMap<String,String> params=new LinkedMultiValueMap<>();
        params.add("username","test1");
        params.add("password","test1");
        restMockMvc.perform(post("/account/create").params(params)).andExpect(status().isOk());
        //覆盖测试,期待失败
        restMockMvc.perform(post("/account/create")
                .param("username","test1")
                .param("password","no"))
                .andExpect(status().isBadRequest()).andDo(print())
                //校验报错信息
                .andExpect(content().string("用户test1已存在"))
                //将详细信息输出到控制台
                .andDo(print());
        //检查数据库（这是多此一举，因为前面测试200成功就肯定是save成功了）,再此只做展示用法
        assertThat(queryUserRepository.count(),is(1L));//1,L长整形

    }

    @Test
    public void updateUser() throws Exception {
        //用户不存在 期望失败
        restMockMvc.perform(post("/account/update")
                .params(new LinkedMultiValueMap(){{add("username","test1");add("password","test1");}})).andExpect(status().isBadRequest());
        //新建用户
        User user=EntityCreate.getnewUser();
        queryUserRepository.save(user);
        //修改用户密码 期望成功
        restMockMvc.perform(post("/account/update")
                .params(new LinkedMultiValueMap(){{add("username",user.getName());add("password","no");}})).andExpect(status().isOk());
    }

    @Test
    public void login() throws Exception {
        //用户不存在 模拟登陆失败
        restMockMvc.perform(post("/account/login")
                .params(new LinkedMultiValueMap(){{add("username","test1");add("password","test1");}})).andExpect(status().isBadRequest());
        //新建用户 使用调用url的方法
        User user = EntityCreate.getnewUser();
        restMockMvc.perform(post("/account/create").params(new LinkedMultiValueMap(){{add("username",user.getName());add("password",user.getPassword());}})).andExpect(status().isOk());
        //模拟登陆成功
        restMockMvc.perform(post("/account/login")
                .params(new LinkedMultiValueMap(){{add("username",user.getName());add("password",user.getPassword());}})).andExpect(status().isOk());
    }
}