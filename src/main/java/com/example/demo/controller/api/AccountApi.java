package com.example.demo.controller.api;

import com.example.demo.Vo.RequestVo.Top;
import com.example.demo.Vo.RequestVo.UserRVo;
import com.example.demo.domain.User;
import com.example.demo.domain.User_;
import com.example.demo.domain.enumeration.TopRankTime;
import com.example.demo.repository.query.QueryUserRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.hibernate.query.criteria.internal.compile.RenderingContext;
import org.hibernate.query.criteria.internal.expression.function.BasicFunctionExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/account")
@Api(tags = "账号Api")
@Transactional(readOnly = true)
public class AccountApi {

    @Autowired
    QueryUserRepository userRepository;
    @Autowired
    EntityManager em;

    @ApiOperation("查询账号list")
    @GetMapping("/list")
    public ResponseEntity<List<User>> listUser(UserRVo userRVo) {
        Specification specification = Specification.<User>where(null);

        specification = specification.and(new Specification() {
            @Override
            public Predicate toPredicate(Root root, CriteriaQuery criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate p1 = criteriaBuilder.and();//创建and谓语
                if (StringUtils.isNotBlank(userRVo.getName())) {//可选查询，StirngUtils检查==null,==""情况
                    p1.getExpressions().add(criteriaBuilder.equal(root.get(User_.name), userRVo.getName()));//User_是JPA元模型pom配置hibernate-jpamodelgen后mvnw clean install 就生成了
                }
                if (StringUtils.isNotBlank(userRVo.getArea())) {
                    p1.getExpressions().add(criteriaBuilder.equal(root.get(User_.area), userRVo.getArea()));
                }
                if (userRVo.getCreatetime() != null) {
                    p1.getExpressions().add(FilterbyTime(root.get(User_.createtime), userRVo.getCreatetime(), criteriaBuilder));
                }
                if (userRVo.getLogintime() != null) {
                    p1.getExpressions().add(FilterbyTime(root.get(User_.logintime), userRVo.getLogintime(), criteriaBuilder));
                }
                return p1;
            }
        });
        List<User> list = userRepository.findAll(specification);
        return ResponseEntity.ok(list);
    }

    @ApiOperation("查询账号page应用分页")
    @GetMapping("/page")
    public ResponseEntity<Page> pageUser(UserRVo userRVo, Pageable pageable) {
        Page<User> page = userRepository.findAll(new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate p1 = StringUtils.isNotBlank(userRVo.getName()) ? criteriaBuilder.equal(root.get(User_.name), userRVo.getName()) : criteriaBuilder.conjunction();
                Predicate p2 = StringUtils.isNotBlank(userRVo.getArea()) ? criteriaBuilder.equal(root.get(User_.area), userRVo.getArea()) : criteriaBuilder.conjunction();
                Predicate p3 = FilterbyTime(root.get(User_.createtime), userRVo.getCreatetime(), criteriaBuilder);
                Predicate p4 = FilterbyTime(root.get(User_.logintime), userRVo.getLogintime(), criteriaBuilder);
                return criteriaBuilder.and(p1, p2, p3, p4);
            }
        }, pageable);
        return ResponseEntity.ok(page);
    }

    @ApiOperation("老用户排行")
    @GetMapping("/top{num}")
    //使用Criteria的例子
    public ResponseEntity<List<Top>> gettop(@PathVariable @RequestParam(defaultValue = "10") Integer num) {
        CriteriaBuilder builder = em.getCriteriaBuilder();//获取builder
        CriteriaQuery<Tuple> query = builder.createTupleQuery();//获取query句柄
        Root<User> root = query.from(User.class);//选择from
        //构建SQL常量（build.Literal会加'',防止sql注入）,重写Expression基础类
        class StaticExpression extends BasicFunctionExpression {

            public StaticExpression(CriteriaBuilderImpl criteriaBuilder, Class javaType, String functionName) {
                super(criteriaBuilder, javaType, functionName);
            }

            @Override
            public String render(RenderingContext renderingContext) {
                return getFunctionName();
            }

        }
        Expression<String> day = new StaticExpression(null, String.class, "DAY");
        Expression<Integer> time = builder.function("TIMESTAMPDIFF", Integer.class, day, root.get(User_.createtime), root.get(User_.logintime));//MYSQL的TIMESTAMPDIFF函数
        //选择输出项
        query.multiselect(
                root.get(User_.name),//名字
                builder.diff(time,0)//老用户的时间,不用diff封装会报错 jpa无法编译query语句
        );
        query.where(builder.isNotNull(time));
        query.orderBy(builder.desc(time));//降序排序
        List<Tuple> tuples = em.createQuery(query).getResultList();
        List<Top> result = new ArrayList<>();
        int rank = 1;
        for (Tuple t : tuples) {
            if (tuples.indexOf(t) != 0 && t.get(1, Integer.class) != result.get(result.size() - 1).getDay()) {//排除top10但是11名和10是一样的积分
                rank++;
            }
            result.add(new Top(rank, t.get(0, String.class), t.get(1, Integer.class)));
            if (rank > num && tuples.indexOf(t)>num) {
                break;
            }
        }
        return ResponseEntity.ok(result);

    }

    //按时间返回筛选条件ZoneId.systemDefault()与ZoneId.of("Asia/Shanghai")等同，中国东8区 GMT+8,Instant是格林尼治时间，比中国时间少8小时
    private Predicate FilterbyTime(Path<Instant> timepath, TopRankTime topRankTime, CriteriaBuilder builder) {
        LocalDate now = LocalDate.now();
        if (topRankTime == null) return builder.conjunction();
        if (topRankTime.equals(TopRankTime.ThisWeek)) {
            //当前日期的周1的上海时间0时
            return builder.between(timepath, now.with(DayOfWeek.MONDAY).atStartOfDay(ZoneId.systemDefault()).toInstant(), Instant.now());//2018-11-17->2018-11-11T16:00:00Z
        } else if (topRankTime.equals(TopRankTime.ThisMonth)) {
            //当前日期的月第一天的上海时间0时
            return builder.between(timepath, now.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant(), Instant.now());//2018-11-17->2018-10-31T16:00:00Z
        } else if (topRankTime.equals(TopRankTime.ThisQuarter)) {
            //当前季度第一天上海时间0时
            return builder.between(timepath, getQuarterTime(now, true), Instant.now());//2018-11-20->2018-09-30T16:00:00Z
        } else if (topRankTime.equals(TopRankTime.ThisYear)) {
            //当前年初1号上海时间0时
            return builder.between(timepath, now.withDayOfYear(1).atStartOfDay(ZoneId.systemDefault()).toInstant(), Instant.now());//2018-11-17->2017-12-31T16:00:00Z
        } else {
            return builder.conjunction();
        }
    }

    //获取季度开始时间
    private Instant getQuarterTime(LocalDate time, boolean isQuarterStart) {
        //按照财报
        int month = time.getMonthValue();
        LocalDate result = time;
        int months[] = {1, 4, 7, 10};
        if (!isQuarterStart) {
            months = new int[]{3, 6, 9, 12};
        }
        //检查是在哪个区间中
        if (month >= 1 && month <= 3)
            month = months[0];
        else if (month >= 4 && month <= 6)
            month = months[1];
        else if (month >= 7 && month <= 9)
            month = months[2];
        else
            month = months[3];
        //决定月初还是月末
        return isQuarterStart ? result.withMonth(month).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant() : result.withMonth(month).withDayOfMonth(1).atStartOfDay(ZoneId.of("Asia/Shanghai")).minus(Duration.ofNanos(1)).toInstant();
    }
}
