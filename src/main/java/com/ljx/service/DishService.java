package com.ljx.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ljx.dto.DishDto;
import com.ljx.entity.Dish;

public interface DishService extends IService<Dish> {

    //新增菜品，同时插入菜品对应的口味数据，需要操作两张表、dish、dishflavor
    public void saveWithFlavor(DishDto dishDto);
}
