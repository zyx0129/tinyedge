package com.example.edgecustomer.model;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

public class System implements Serializable {
    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;
    private Integer userId;
    private String name;
    private String custom;
    private String router;
    private String description;
    private String status;   //0新建 1更新

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCustom() {
        return custom;
    }

    public void setCustom(String custom) {
        this.custom = custom;
    }

    public String getRouter() {
        return router;
    }

    public void setRouter(String router) {
        this.router = router;
    }
}
