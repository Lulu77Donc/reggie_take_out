package com.ljx.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ljx.dto.SetmealDto;
import com.ljx.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    //新增套餐，同时保存套餐和菜品的关联关系
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐，同时需要删除套餐和菜品关联数据
     * @param ids
     */
    public void removeWithDish(List<Long> ids);
}
