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

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/message")
@Api(tags = "消息Api")
public class MessageApi {
    @Autowired AccountApi accountApi;
    @Autowired private QueryMessageRepository queryMessageRepository;
    @Autowired private EntityManager em;

    //解决无法接收Instant类型问题
    @ModelAttribute
    Instant initInstant() {
        return Instant.now();
    }

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

    //使用原生sql查询
    @ApiOperation("用户说说发布统计")
    @GetMapping("/saytop{num}")
    public ResponseEntity<List<Tuple>> saytop(@ApiParam(value = "统计前几",defaultValue = "10") @PathVariable Integer num){
        //使用原生sql语句，选择用户名字和用户名字出现总数，选择user和message表并定义别名u和m,使用用户id为内连接标志，按用户名分组，按用户名出现次数倒序排序，限制查询个数num
        //这么使用存在有一个sql注入点limit，理论来说springboot会过滤和筛选最终num一定会是整型所以sql注入安全，但是为了以防万一不建议这么做，可以查出来以后筛选,或者getResultList之前.setMaxResults(num)
        //使用Tuple元组的优势是不定义个数，但缺点是没有使用@ApiModelProperty("")，在swagger文档里没有响应字段的备注，对于增加前后端沟通成本，可以新建并转换成一个带备注注解list视图再返回
        List<Tuple> result=em.createNativeQuery("select u.name,count(u.name) from user u join message m where m.user_id=u.id group by u.name order by count(u.name) desc limit "+num+";").getResultList();
        return ResponseEntity.ok(result);
    }

    //使用jpa repository原生sql语句
    @ApiOperation("查看今天发布的说说")
    @GetMapping("/today")
    public ResponseEntity<List<Message>> today(){
        return ResponseEntity.ok(queryMessageRepository.today());
    }

}
