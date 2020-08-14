package com.example.edgecustomer.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.edgecustomer.service.RouterService;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class RouterServiceImpl implements RouterService {

    //private JSONObject modulePubJson;
    private JSONObject pubSubJson;
    private JSONObject topicJson;

    @Override
    public String generateRouter(String routeJsonStr) {
        //modulePubJson = new JSONObject();
        if(routeJsonStr.isEmpty()) return null;
        pubSubJson = new JSONObject();
        topicJson = new JSONObject();
        JSONObject routerObj = JSONObject.parseObject(routeJsonStr);
        for (Map.Entry<String, Object> entry : routerObj.entrySet()) {
            String deviceName = entry.getKey();
             List<Map<String,Object>> routerList =(List<Map<String,Object>>) entry.getValue();
             for(Map<String,Object> routerInstance:routerList) {
                 String moduleName = routerInstance.get("moduleName").toString();
                 List nextRoute = (List<Map<String, Object>>) routerInstance.get("next");
                 getNextRoute(nextRoute, moduleName, deviceName);
             }
        }

        /*JSONArray jsonArray = JSONObject.parseArray(routeJsonStr);
        for (Iterator iterator = jsonArray.iterator(); iterator.hasNext(); ) {
            JSONObject jsonObject = (JSONObject) iterator.next();
            String deviceName = jsonObject.getString("deviceName");
            JSONObject routeObj = jsonObject.getJSONObject("route");
            String moduleName = routeObj.getString("moduleName");
            JSONArray nextRoute = routeObj.getJSONArray("moduleName");
            getNextRoute(nextRoute, moduleName, deviceName);
        }*/

        String topicJsonStr = topicJson.toJSONString();
        String pubSubStr = pubSubJson.toJSONString();
        System.out.println(topicJsonStr);
        System.out.println(pubSubStr);
        //优化 一个订阅者如果订阅了 多个topic  topic可能可以合并
        JSONObject joinTopic = new JSONObject();
        for (Map.Entry<String, Object> entry : topicJson.entrySet()) {
            List<String> subList = (List<String>) entry.getValue();
            String topic = entry.getKey();
            String key = StringUtils.join(subList, '_');
            JSONArray topicArray;
            if (joinTopic.containsKey(key)) {
                topicArray = joinTopic.getJSONArray(key);
            } else {
                topicArray = new JSONArray();
                joinTopic.put(key,topicArray);
            }
            topicArray.add(topic);
        }
        JSONObject topicReplace = new JSONObject();
        for (Map.Entry<String, Object> entry : joinTopic.entrySet()) {
            List<String> topicList = (List<String>) entry.getValue();
            if (topicList.size() > 1) {
                String newTopic = StringUtils.join(topicList, '_');
                for (String topic : topicList) {
                    topicReplace.put(topic, newTopic);
                }
            }
        }
        //合并
        for (Map.Entry<String, Object> entry : pubSubJson.entrySet()) {
            Map<String, Object> pubSubInstance = (Map<String, Object>) entry.getValue();
            if(pubSubInstance.containsKey("pub")){
                Map<String, String> pubList= (Map<String, String>)pubSubInstance.get("pub");
                for(Map.Entry<String, String> pubEntry:pubList.entrySet()){
                    String topic = pubEntry.getValue();
                    if(topicReplace.containsKey(topic)){
                        pubList.replace(pubEntry.getKey(),topicReplace.getString(topic));
                    }
                }
               /* List<Map<String, String>> pubList= (List<Map<String, String>>)pubSubInstance.get("pub");
                for(Map<String, String> pubInstance:pubList){
                    String = pubInstance.get("topic");
                    if(topicReplace.containsKey(topic)){
                        pubInstance.replace("topic",topicReplace.getString(topic));
                    }
                }*/
            }
            if(pubSubInstance.containsKey("sub")){
                List<String> subList= (List<String>)pubSubInstance.get("sub");
                List<String> removeList = new ArrayList<>();
                List<String> addList = new ArrayList<>();
                for(String topic:subList){
                    if(topicReplace.containsKey(topic)){
                        removeList.add(topic);
                        String newTopic = topicReplace.getString(topic);
                        if(!addList.contains(newTopic)){
                            addList.add(newTopic);
                        }
                    }
                }
                if(!removeList.isEmpty()){
                    subList.removeAll(removeList);
                    subList.addAll(addList);
                }
            }
            //entry.getValue()
        }
        topicJsonStr = topicJson.toJSONString();
        pubSubStr = pubSubJson.toJSONString();
        System.out.println(topicJsonStr);
        System.out.println(pubSubStr);
        return pubSubStr;
    }



    private void getNextRoute(List<Map<String,Object>> nowRoute,String moduleName,String deviceName) {
        String topic = moduleName + "_" + deviceName;
        JSONObject pubInstance;
        JSONObject pubObj;
        if(pubSubJson.containsKey(moduleName)){   //如果已经有module
            pubInstance = pubSubJson.getJSONObject(moduleName);
            if(pubInstance.containsKey("pub")){
                pubObj = pubInstance.getJSONObject("pub");
            }else{
                pubObj = new JSONObject();
                pubInstance.put("pub",pubObj);
            }
        }else{
            pubInstance = new JSONObject();
            pubObj = new JSONObject();
            pubInstance.put("pub",pubObj);
            pubSubJson.put(moduleName,pubInstance);
        }
        //pubObj.put("topic",topic);
        //pubObj.put("condition",deviceName);
        pubObj.put(deviceName,topic);
        JSONArray topicArray = new JSONArray();
        topicJson.put(topic,topicArray);
        //for (Iterator iterator = routeArray.iterator(); iterator.hasNext();) {
        for(Map<String,Object> nextModule:nowRoute){
            //JSONObject jsonObject = (JSONObject) iterator.next();
            String nextModuleName = nextModule.get("moduleName").toString();
            JSONObject subInstance;
            JSONArray subArray;
            topicArray.add(nextModuleName);
            if(pubSubJson.containsKey(nextModuleName)){
                subInstance = pubSubJson.getJSONObject(nextModuleName);
                if(subInstance.containsKey("sub")){
                    subArray = subInstance.getJSONArray("sub");
                }else{
                    subArray = new JSONArray();
                    subInstance.put("sub",subArray);
                }
            }else{
                subInstance = new JSONObject();
                pubSubJson.put(nextModuleName,subInstance);
                subArray = new JSONArray();
                subInstance.put("sub",subArray);
            }
            subArray.add(topic);
            List<Map<String,Object>> nextRoute = (List<Map<String,Object>>)nextModule.get("next");
            if(!nextRoute.isEmpty()){
                getNextRoute(nextRoute,nextModuleName, deviceName);
            }
        }
    }



}
