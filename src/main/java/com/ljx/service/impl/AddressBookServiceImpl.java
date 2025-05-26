package com.ljx.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ljx.entity.AddressBook;
import com.ljx.mapper.AddressBookMapper;
import com.ljx.service.AddressBookService;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

}
