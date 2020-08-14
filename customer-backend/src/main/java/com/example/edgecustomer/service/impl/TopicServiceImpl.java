package com.example.edgecustomer.service.impl;

import com.example.edgecustomer.mapper.TopicMapper;
import com.example.edgecustomer.model.Topic;
import com.example.edgecustomer.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TopicServiceImpl implements TopicService {
    @Autowired
    TopicMapper topicMapper;

    @Override
    public void addTopic(Integer deviceId, String topicName){
        Topic topic = new Topic();
        topic.setTopic(topicName);
        topic.setDeviceId(deviceId);
        topic.setAccess(3);
        topicMapper.insertSelective(topic);
    }

}
