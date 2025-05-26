package com.ljx.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ljx.entity.User;
import com.ljx.mapper.UserMapper;
import com.ljx.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


}
