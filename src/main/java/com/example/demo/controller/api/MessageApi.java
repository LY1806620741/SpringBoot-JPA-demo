package com.example.demo.controller.api;

import com.example.demo.domain.Message;
import com.example.demo.domain.Message_;
import com.example.demo.domain.User_;
import com.example.demo.domain.enumeration.TopRankTime;
import com.example.demo.repository.query.QueryMessageRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/message")
@Api(tags = "消息Api")
public class MessageApi {
    @Autowired AccountApi accountApi;
    @Autowired private QueryMessageRepository queryMessageRepository;

    //解决无法接收Instant类型问题
    @ModelAttribute
    Instant initInstant() {
        return Instant.now();
    }
//    动态改变时间，失败
//    public MessageApi() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
//        setAnnotationValue(this.getClass().getMethod("countbycursor",Instant.class,Instant.class).getParameterAnnotations()[0][0],"defaultValue",Instant.now());
//    }
    @GetMapping("/list")
    @ApiOperation("说说列表")
    public ResponseEntity<List<Message>> list(@ApiParam("按用户id查看")@RequestParam(required = false) Long userid,@ApiParam("按发表时间查看") @RequestParam(required = false)TopRankTime saytime,@ApiParam("按发表内容查看") String saydata){
        Specification<Message> specification = Specification.where( null );
        if (userid!=null) {
            specification = specification.and((root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.join(Message_.user).get(User_.id),userid));
        }
        if (saytime!=null){
            specification = specification.and((root, criteriaQuery, criteriaBuilder) -> accountApi.FilterbyTime(root.get(Message_.time),saytime,criteriaBuilder));
        }
        if (StringUtils.isNotBlank(saydata)){
            specification = specification.and((root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.like(root.get(Message_.value),"%"+saydata+"%"));
        }
        return ResponseEntity.ok(queryMessageRepository.findAll(specification));
    }

    @GetMapping("/count")
    @ApiOperation("说说计数")
    public ResponseEntity<Integer> count(@ApiParam("按用户计数") @RequestParam(required = false) Long userid, @ApiParam("按发表时间计数")@RequestParam(required = false)TopRankTime saytime){
         return ResponseEntity.ok(queryMessageRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
             Predicate p=criteriaBuilder.and(criteriaBuilder.conjunction());//创建一个and合集,填入一个1以防空and连接
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

    @GetMapping("/countbyuser/{id}")
    @ApiOperation("查看用户发表的说说总数")
    //使用repository方法查询
    public ResponseEntity<Long> countbyuser(@ApiParam("用户id")@PathVariable Long id){
        return ResponseEntity.ok(queryMessageRepository.countByUser_Id(id));
    }

    @GetMapping("/countbycursor")
    @ApiOperation(value = "左右游标计数",notes = "例如在2018-12-12(左游标),2018-12-13(右游标)之间")
    public ResponseEntity<Long> countbycursor(@ApiParam(value = "左游标",defaultValue = "2018-12-19T09:06:43.145Z")@RequestParam(required = false) @ModelAttribute Instant left,@RequestParam(required = false) @ApiParam("右游标")@ModelAttribute Instant right) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
        //使用count统计符合条件的条数
        return ResponseEntity.ok(queryMessageRepository.count(((root, criteriaQuery, criteriaBuilder) -> {
            Predicate p=criteriaBuilder.and();
            p.getExpressions().add(left!=null?criteriaBuilder.greaterThanOrEqualTo(root.get(Message_.time),left):criteriaBuilder.conjunction());
            p.getExpressions().add(right!=null?criteriaBuilder.lessThanOrEqualTo(root.get(Message_.time),right):criteriaBuilder.conjunction());
            return p;
        })));
    }
//   反射改变注解，失败
//    public static void setAnnotationValue(Annotation annotation, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
//        InvocationHandler handler = Proxy.getInvocationHandler(annotation);
//        Field hField = handler.getClass().getDeclaredField("memberValues");
//        hField.setAccessible(true);
//        Map memberValues = (Map) hField.get(handler);
//        memberValues.put(fieldName, value);
//    }

}
