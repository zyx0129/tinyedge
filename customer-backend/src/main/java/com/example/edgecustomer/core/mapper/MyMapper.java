package com.example.edgecustomer.core.mapper;

import tk.mybatis.mapper.common.*;

public interface MyMapper<T> extends Mapper<T>,MySqlMapper<T>,IdsMapper<T>,ConditionMapper<T>,ExampleMapper<T>{
}
