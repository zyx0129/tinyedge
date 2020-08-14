package com.example.edgecustomer.model;

import javax.persistence.Id;
import java.io.Serializable;

public class Function implements Serializable {
    @Id
    private Integer id;
    private Integer userId;
    private String name;
    private String description;
    private String kubernetes;
    private String compose;
    private String type;
    private String status;   //0新建 1更新
    private String content;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
}
