package com.ljx.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ljx.dto.DishDto;
import com.ljx.entity.Dish;

public interface DishService extends IService<Dish> {

    //新增菜品，同时插入菜品对应的口味数据，需要操作两张表、dish、dishflavor
    public void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品，口味表
    public DishDto getByIdWithFlavor(Long id);

    //更新菜品信息，同时更新对应的口味信息
    public void updateWithFlavor(DishDto dishDto);
}
