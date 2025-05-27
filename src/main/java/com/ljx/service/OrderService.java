package com.ljx.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ljx.entity.Orders;

public interface OrderService extends IService<Orders> {

    public void submit(Orders orders);
}
