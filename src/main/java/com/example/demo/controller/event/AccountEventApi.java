package com.example.demo.controller.event;

import com.example.demo.domain.User;
import com.example.demo.domain.User_;
import com.example.demo.repository.query.QueryUserRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping(value = "/account",produces = "application/json;charset=utf-8")//默认返回编码是utf-8
@Api(tags = "账号Event")
public class AccountEventApi {

    //使用Set注入，Field注入(@Autowired private QueryUserRepository userRepository;)会有Warning,
    private QueryUserRepository userRepository;

    @Autowired
    public void setUserRepository(QueryUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ApiOperation("创建账号")
    @PostMapping(value = "/create")
    public ResponseEntity<String> createUser(@ApiParam("用户名") @RequestParam String username,
                                             @ApiParam("密码") @RequestParam String password,
                                             @ApiParam("地区") @RequestParam(required = false) String area
    ){
        if(userRepository.findOneByName(username).isPresent()){
            return ResponseEntity.badRequest().body("用户"+username+"已存在");//有提示的400
        }
        User user=new User();
        user.setName(username);
        user.setPassword(password);//直接明文密码了，强迫症用PasswordEncoder加密一下,org.springframework.security的
        user.setArea(area);
        userRepository.save(user);
        return ResponseEntity.ok("用户"+username+"添加成功");//有提示的200回答
    }

    @ApiOperation("修改密码")
    @PostMapping("/update")
    public ResponseEntity<String> updateUser(@ApiParam("用户名") @RequestParam String username,
                                             @ApiParam("密码") @RequestParam String password){
        Optional<User> user=userRepository.findOneByName(username);
        if(user.isPresent()){
            user.get().setPassword(password);
            return ResponseEntity.ok().build();//无提示的页面状态200
        }else {
            return ResponseEntity.badRequest().build();//无提示的页面报错400
        }
    }

    @ApiOperation("模拟登陆")//做登陆功能可以用session或者spring security,在这里就不做了
    @PostMapping("/login")
    public ResponseEntity<String> login(@ApiParam("用户名") @RequestParam String username,
                                        @ApiParam("密码") @RequestParam String password){
        Optional<User> user=userRepository.findOne(Specification.where((root,query,build)-> build.and(build.equal(root.get(User_.name),username),build.equal(root.get(User_.password),password))));

        if (user.isPresent()){
            user.get().setLogintime(Instant.now());//设置登陆时间
            userRepository.save(user.get());//如果jpa是默认持久映射的managed改动直接映射到数据库，如果是分离 detached的需要save
            return ResponseEntity.ok("登陆成功token");
        }else{
            return ResponseEntity.badRequest().body("登陆失败");
        }
    }


}
