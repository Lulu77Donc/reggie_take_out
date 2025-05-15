package com.ljx.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ljx.entity.Category;

public interface CategoryService extends IService<Category> {
    public void remove(Long id);
}
