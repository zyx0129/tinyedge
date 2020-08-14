package com.example.edgecustomer.service;

import com.example.edgecustomer.model.User;


public interface UserService {
    public User getUser(String username);
    public void register(String username,String password);

}
