package com.example.demo.repository.query;

import com.example.demo.domain.Message;
import com.example.demo.repository.MessageRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QueryMessageRepository extends MessageRepository {
    Long countByUser_Id(Long id);
    //select * from message m where m.time>curdate() 今天凌晨以后发的说说
    //select * from message m where m.time>date_add(curdate(),interval extract(hour from now()) hour) 不满一小时前发的说说 当前时间 9.30 那么标志时间是9.00而不是8.30
    //不使用nativequery=true是混合query，使用原生sql的缺点是数据库版本不能频繁切换，因为一些数据库函数对于不用的数据库是不一样的
    @Query(value = "select * from message m where m.time>curdate()",nativeQuery = true)
    List<Message> today();
}
