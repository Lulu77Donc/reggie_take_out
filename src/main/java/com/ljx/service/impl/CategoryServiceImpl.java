package com.ljx.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ljx.common.CustomException;
import com.ljx.entity.Category;
import com.ljx.entity.Dish;
import com.ljx.entity.Setmeal;
import com.ljx.mapper.CategoryMapper;
import com.ljx.service.CategoryService;
import com.ljx.service.DishService;
import com.ljx.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;
    /*
    * 根据id删除分类*/
    public void remove(Long id){
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据分类id查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        long count1 = dishService.count(dishLambdaQueryWrapper);

        //查询当前分类是否关联菜品，如果已经关联，则抛出一个业务异常
        if(count1 > 0){
            //已经关联菜品，抛出一个业务异常
            //这个业务异常为自定义异常，调用父类.msg()构造函数创建
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }

        //查询当前分类是否关联套餐，如果关联，抛出异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper=new LambdaQueryWrapper<>();
        //添加查询条件，根据分类id查询
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        long count2 = setmealService.count(setmealLambdaQueryWrapper);
        if(count2 > 0){
            //已经关联套餐，抛出一个业务异常
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }

        //正常删除分类
        //注意！！这里也可以直接注入categoryService来删除，
        // 但是由于这个类也继承了ServiceImpl，同时也继承IService，所以直接调用父类IService中的方法也可以
        // 实际上categoryService也是同理
        super.removeById(id);
    }

}
