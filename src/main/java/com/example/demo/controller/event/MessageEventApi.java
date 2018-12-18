package com.example.demo.controller.event;

import com.example.demo.domain.Message;
import com.example.demo.domain.User;
import com.example.demo.repository.query.QueryMessageRepository;
import com.example.demo.repository.query.QueryUserRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/message")
@Api(tags = "消息Event")
public class MessageEventApi {
    private QueryMessageRepository messageRepository;
    private QueryUserRepository userRepository;

    public MessageEventApi(QueryMessageRepository messageRepository, QueryUserRepository userRepository){
        this.messageRepository=messageRepository;
        this.userRepository=userRepository;
    }
    @ApiOperation("发说说")
    @PostMapping("/say")
    public ResponseEntity<String> say(@ApiParam("用户id") @RequestParam Long id,
                                      @ApiParam("内容") @RequestParam String data){
        Optional<User> user=userRepository.findById(id);
        if (user.isPresent()){
                messageRepository.save(new Message(user.get(),data));
                return ResponseEntity.ok(user.get().getName()+"的说说发表成功");
        }else{
            return ResponseEntity.badRequest().body("ID为"+id+"的用户不存在");
        }
    }
    @ApiOperation("删除说说")
    @GetMapping("/delete/{id}")
    public ResponseEntity<String> delete(@ApiParam("说说id") @PathVariable Long id){
        Optional<Message> message=messageRepository.findById(id);
        if (message.isPresent()){
            messageRepository.delete(message.get());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON_UTF8).body("删除成功");//contentType决定浏览器收到的header是application/json;charset=utf-8,解决中文乱码
        }else{
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON_UTF8).body("该说说不存在");
        }
    }
}
