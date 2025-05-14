package com.ljx.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/*
* 自定义原数组对象处理器*/
@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
    //这里metaObject实际上是元数据
    /*
    * 插入操作，自动填充*/
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("公共字段自动填充[insert]...");
        log.info(metaObject.toString());
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime",LocalDateTime.now());
        metaObject.setValue("createUser",new Long(1));//由于我们拿不到request请求中的session，无法获得当前用户的id，这里先用固定属性
        metaObject.setValue("updateUser",new Long(1));
    }

    /*
    * 更新操作，自动填充*/
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共字段自动填充[update]...");
        log.info(metaObject.toString());

        long id = Thread.currentThread().getId();
        log.info("线程id为：{}",id);

        metaObject.setValue("updateTime",LocalDateTime.now());
        metaObject.setValue("updateUser",new Long(1));
    }
}
