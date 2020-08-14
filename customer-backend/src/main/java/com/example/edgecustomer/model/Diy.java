package com.example.edgecustomer.model;

import javax.persistence.Id;
import java.io.Serializable;

public class Diy implements Serializable {
    @Id
    private Integer id;
    private Integer userId;
    private String name;
    private String description;
    private String type;
    private String kubernetes;   //0新建 1更新
    private String compose;
    private String core;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKubernetes() {
        return kubernetes;
    }

    public void setKubernetes(String kubernetes) {
        this.kubernetes = kubernetes;
    }

    public String getCompose() {
        return compose;
    }

    public void setCompose(String compose) {
        this.compose = compose;
    }

    public String getCore() {
        return core;
    }

    public void setCore(String core) {
        this.core = core;
    }
}
