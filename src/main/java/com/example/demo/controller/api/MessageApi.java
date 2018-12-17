package com.example.demo.controller.api;

import com.example.demo.domain.Message;
import com.example.demo.domain.Message_;
import com.example.demo.domain.User_;
import com.example.demo.domain.enumeration.TopRankTime;
import com.example.demo.repository.query.QueryMessageRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.persistence.criteria.Predicate;
import java.util.List;

@RestController
@RequestMapping("/message")
@Api(tags = "消息Api")
public class MessageApi {
    @Autowired AccountApi accountApi;
    @Autowired private QueryMessageRepository queryMessageRepository;
    @GetMapping("/list")
    @ApiOperation("说说列表")
    public ResponseEntity<List<Message>> list(@RequestParam(required = false) Long userid, @RequestParam(required = false)TopRankTime saytime,String saydata){
        Specification<Message> specification = Specification.where( null );
        if (userid!=null) {
            specification = specification.and((root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.join(Message_.user).get(User_.id),userid));
        }
        if (saytime!=null){
            specification = specification.and((root, criteriaQuery, criteriaBuilder) -> accountApi.FilterbyTime(root.get(Message_.time),saytime,criteriaBuilder));
        }
        if (StringUtils.isNotBlank(saydata)){
            specification = specification.and((root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.like(root.get(Message_.value),saydata));
        }
        return ResponseEntity.ok(queryMessageRepository.findAll(specification));
    }

    @GetMapping("/count")
    @ApiOperation("说说计数")
    public ResponseEntity<Integer> count(@RequestParam(required = false) Long userid, @RequestParam(required = false)TopRankTime saytime){
         return ResponseEntity.ok(queryMessageRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
             Predicate p=criteriaBuilder.and();//创建一个and合集
             //添加条件
             if (userid!=null) {
                 p.getExpressions().add(criteriaBuilder.equal(root.join(Message_.user).get(User_.id),userid));
             }
             if (saytime!=null){
                 p.getExpressions().add(accountApi.FilterbyTime(root.get(Message_.time),saytime,criteriaBuilder));
             }
             return p;
         }).size());
    }
}
