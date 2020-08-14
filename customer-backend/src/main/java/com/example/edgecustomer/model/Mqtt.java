package com.example.edgecustomer.model;


import java.io.Serializable;

public class Mqtt implements Serializable {
    private String action;
    private String client_id;
    private String username;
    private String keepalive;
    private String ipaddress;
    private String proto_ver;
    private String connected_at;
    private String conn_ack;
    private String reason;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getKeepalive() {
        return keepalive;
    }

    public void setKeepalive(String keepalive) {
        this.keepalive = keepalive;
    }

    public String getIpaddress() {
        return ipaddress;
    }

    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    public String getProto_ver() {
        return proto_ver;
    }

    public void setProto_ver(String proto_ver) {
        this.proto_ver = proto_ver;
    }

    public String getConnected_at() {
        return connected_at;
    }

    public void setConnected_at(String connected_at) {
        this.connected_at = connected_at;
    }

    public String getConn_ack() {
        return conn_ack;
    }

    public void setConn_ack(String conn_ack) {
        this.conn_ack = conn_ack;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
