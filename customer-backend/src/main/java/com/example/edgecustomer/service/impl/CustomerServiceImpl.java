package com.example.edgecustomer.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.edgecustomer.model.Diy;
import com.example.edgecustomer.model.Function;
import com.example.edgecustomer.model.Module;
import com.example.edgecustomer.model.User;
import com.example.edgecustomer.service.*;
import com.example.edgecustomer.utils.CompileException;
import com.example.edgecustomer.utils.FileOperater;
import com.fasterxml.jackson.databind.JsonSerializer;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;
import tk.mybatis.mapper.genid.GenId;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    FunctionService functionService;
    @Autowired
    ModuleService moduleService;
    @Autowired
    UserService userService;
    @Autowired
    DiyService diyService;
    @Autowired
    TemplateService templateService;

    @Value("${fileLibPath}")
    private String fileLibPath;
    @Value("${userPath}")
    private String userPath;
    private Set<String> serviceList;
    //private JSONObject customJson;
    private JSONObject moduleInfoJson;
    private JSONObject moduleConfigJson;
    private JSONObject sceneJson;

    //private String userLibPath;
    private String outputPath;
    private Map yamlMap;
    private String core = "docker-compose";
    private Integer userId;

    @Override
    public JSONObject init(String userName,String systemName,String customString) throws FileNotFoundException, CompileException {
        outputPath = userPath + "/" + userName +"/" + systemName+"/system";
        //userLibPath = userPath + "/" + userName +"/userLib";
        //outputPath = workPath + "/" + "system";
        JSONObject customJson = new JSONObject();
        userId = userService.getUser(userName).getId();
        try{
            customJson = JSONObject.parseObject(customString);
            moduleInfoJson = customJson.getJSONObject("serviceInfo");
            moduleConfigJson = customJson.getJSONObject("serviceConfig");
            sceneJson = customJson.getJSONObject("scene");
            serviceList = moduleInfoJson.keySet();
        }catch (Exception e){
            throw new CompileException("The file formot of custom.json is incorrect");
        }

        /*JSONObject moduleList = customJson.getJSONObject("serviceList");
        //Set<String> services = customJson.keySet();
        JSONArray systemArray = moduleList.getJSONArray("system");
        JSONArray startArray = moduleList.getJSONArray("start");
        JSONArray middleArray = moduleList.getJSONArray("middle");
        JSONArray endArray = moduleList.getJSONArray("end");
        JSONArray aloneArray = moduleList.getJSONArray("alone");
        JSONArray moduleArray = new JSONArray();
        if(systemArray!=null){
            moduleArray = moduleArray.fluentAddAll(systemArray);
        }
        if(startArray!=null){
            moduleArray = moduleArray.fluentAddAll(startArray);
        }
        if(middleArray!=null){
            moduleArray = moduleArray.fluentAddAll(middleArray);
        }
        if(endArray!=null){
            moduleArray = moduleArray.fluentAddAll(endArray);
        }
        if(aloneArray!=null){
            moduleArray = moduleArray.fluentAddAll(aloneArray);
        }
        serviceList = moduleArray.toJavaList(String.class);*/
        if(!customJson.containsKey("scene")||!sceneJson.containsKey("core") || sceneJson.getString("core").equals("docker-compose")){
            //读取 docker-compose 文件头部通用信息
            core = "docker-compose";
            Yaml yaml=new Yaml();
            String composeYaml = templateService.getTemplate("dockerCompose");
            yamlMap= yaml.load(composeYaml);
            //File rootYaml=new File(fileLibPath + "/docker-compose.yaml");
            //yamlMap=(Map)yaml.load(new FileInputStream(rootYaml));
        }else if(sceneJson.getString("core").equals("kubernetes")){
            core = "kubernetes";
        }else{
            throw new CompileException("An illegal core of "+ sceneJson.getString("core")+ " was set, tinyedge currently supports cores of docker-compose and kubernetes");
        }
        JSONObject moduleList =  getModuleList(userName);
        File outFile = new File(outputPath);
        if(outFile.exists()){
            FileOperater.deleteDir(outputPath);
        }
        outFile.mkdirs();
        return moduleList;
    }

    private JSONObject getModuleList(String userName) throws CompileException {
        Map map = new HashMap();
        List<String> systemList = new ArrayList<>();
        List<String> startList = new ArrayList<>();
        List<String> middleList = new ArrayList<>();
        List<String> endList = new ArrayList<>();
        List<String> aloneList = new ArrayList<>();
        User user = userService.getUser(userName);
        Integer userId = user.getId();
        for (String service : serviceList) {
            String source = moduleInfoJson.getString(service);
            String type ="";
            if (source.equals("system")) {
                type = moduleService.getModuleType(service);
            } else if(source.equals("fc")) {
                type = functionService.getModuleType(service, userId);
            } else if(source.equals("diy")){
                type = diyService.getModuleType(service, userId);
            } else{
                throw new CompileException("Service "+service+" of type "+source+" could not be found");
            }
            if(type == null){
                throw new CompileException("Service "+service+" of type "+source+" could not be found");
            }
            switch (type) {
                case "system":
                    systemList.add(service);
                    break;
                case "start":
                    startList.add(service);
                    break;
                case "middle":
                    middleList.add(service);
                    break;
                case "end":
                    endList.add(service);
                    break;
                case "alone":
                    aloneList.add(service);
                    break;
            }
        }
        map.put("system",systemList);
        map.put("start",startList);
        map.put("middle",middleList);
        map.put("end",endList);
        map.put("alone",aloneList);
        JSONObject moduleList = new JSONObject(map);
        return moduleList;
    }

    @Override
    public void handle() throws Exception {
        Map services=new HashMap<String,Map>();
        for(String service:serviceList){
            System.out.println("handle service "+service);
            String source = moduleInfoJson.getString(service);
            String compose = "";
            String kubernetes = "";
            if(source.equals("system")){
                //serviceDirPath = fileLibPath + "/"+ service;
                Module module = moduleService.getModule(service);
                compose = module.getCompose();
                kubernetes = module.getKubernetes();
            }else if(source.equals("fc")){
                //serviceDirPath = userLibPath+"/"+ service;
                //处理 函数计算文件
                //String content = FileOperater.readFile(serviceDirPath+"/msgRec.py");
                Function function = functionService.getFunction(userId,service);
                String content = function.getContent();
                kubernetes = function.getKubernetes();
                compose = function.getCompose();
                FileOperater.writeFile(content,outputPath + "/" +service+"/msgRec.py");
            }else if(source.equals("diy")){
                //serviceDirPath = userLibPath+"/"+ service;
                Diy diy = diyService.getDiy(userId,service);
                kubernetes = diy.getCompose();
                compose = diy.getCompose();
            }
            if(core.equals("docker-compose")){
                if (compose == null) {  //没有docker-compose
                    throw new CompileException("The service " + service + " does not reference the docker-compose file");
                }
                compose = preprocess(compose);
                try {
                    Yaml yaml=new Yaml();
                    Map yamlContent = (Map) yaml.load(compose);
                    services.putAll((Map) yamlContent.get("services"));
                } catch (Exception e) {
                    throw new CompileException("The docker-compose file format of service "+ service + " is incorrect");
                }
            }else if(core.equals("kubernetes")){
                if (kubernetes == null) {  //没有k8s
                    throw new CompileException("The service " + service + " does not reference the k8s file");
                }
                kubernetes = preprocess(kubernetes);
                FileOperater.writeFile(kubernetes,outputPath + "/" +service+"/k8s.yaml");
            }


                /*String files[] = serviceDir.list();
                for(String imagefile : files){
                    if(!imagefile.equals("docker-compose.yaml") ){
                        String imageDirPath = serviceDirPath+"/"+imagefile;
                        File imageDir = new File(imageDirPath);
                        if(imageDir.isDirectory()){
                            String dynamicLibPath = imageDirPath+ "/dynamicLib";
                            String staticLibPath = imageDirPath+ "/staticLib";
                            //处理动态文件
                            File dynamicLib = new File(dynamicLibPath);
                            if(dynamicLib.exists()){
                                String relativePath = service +"/"+ imagefile +"/dynamicLib";
                                if(source.equals("system")){
                                    traversal(composeLib,relativePath); //处理该服务下的所有文件，并将结果输出到指定目录
                                }else if(source.equals("user")) {
                                    traversal(userLibPath,relativePath); //处理该服务下的所有文件，并将结果输出到指定目录
                                }
                            }
                            //拷贝静态文件
                            File staticLib = new File(staticLibPath);
                            if(staticLib.exists()){
                                FileOperater.copyDir(staticLibPath,outputPath + "/" + service +"/"+ imagefile + "/staticLib");
                            }
                        }
                    }
                }*/
        }
        if(core.equals("docker-compose")){
            yamlMap.put("services",services);
            FileOperater.writeFile(new Yaml().dump(yamlMap),outputPath + "/" +"docker-compose.yaml");
        }
    }

    @Override
    public String preprocess(String str) throws Exception {
        /*List<String> list=new ArrayList<>();
        list.add("firstservice");
        list.add("sencondservice");
        list.add("fifthservice");*/
        //JSONArray serviceList =json.getJSONArray("serviceList");
        //List<String> serviceList=JSONObject.parseArray(jsonArray.toString(),String.class);

        int stage=0;
        String condition="";
        String fstr="";
        int count=0;
        for(int i=0;i<str.length();i++) {
            char ch=str.charAt(i);
            if(stage==0){     //正常读
                if(ch=='%'&&str.charAt(i+1)=='$') {    //读状态   IF(@service) IF(service1~service2)
                    stage=1;
                    i++;
                }else if(ch=='%'&&str.charAt(i+1)=='}'){   //读有效状态结束
                    i++;
                }else{
                    fstr+=ch;
                }
            }else if(stage==1){    //读状态
                if(ch=='%'&&str.charAt(i+1)=='{'){
                    i++;
                    if(checkCondition(condition)){   //判断状态是否有效
                        stage=0;
                    }else{
                        stage=2;
                        count=1;
                    }
                    condition ="";
                }else{
                    condition+=ch;
                }
            }else if(stage==2){     //无效读
                if(ch=='%'){
                    if(str.charAt(i+1)=='{'){
                        i++;
                        count++;
                    }else if(str.charAt(i+1)=='}'){
                        i++;
                        count--;
                        if(count==0){
                            stage=0;
                        }
                    }
                }
            }
        }

        //正则匹配配置
        String REGEX = "%#([A-Za-z0-9\\.\\-]+)";
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            String configStr="";
            try {
                configStr = getConfig(matcher.group(1));
            }catch (Exception e){
                throw new CompileException(matcher.group(1)+" can not match any config");
            }
            fstr = fstr.replace(matcher.group(),configStr);
        }
        return fstr;
    }

    private String getConfig(String str) {
        String[] list=str.split("\\.");
        JSONObject serviceConfig=moduleConfigJson.getJSONObject(list[0]);

        for(int i=1;i<list.length;i++){
            /*if(!serviceConfig.containsKey(list[i])){
                throw new CompileException(str+" can not match any config");
                //System.out.println(str+" can not match any config");
                //return "false";
            }*/
            if(i==list.length-1){
                String configStr=serviceConfig.getString(list[i]);
                System.out.println(configStr);
                return  configStr;
            }else{
                serviceConfig=serviceConfig.getJSONObject(list[i]);
            }
        }

           /* if(list.contains(matcher.group(1))){
                System.out.println(matcher.group(1));
            }*/

        return  str;



    }


    private boolean checkCondition(String condition) throws Exception {    // && || ==

        //JSONArray serviceList =json.getJSONArray("serviceList");


        //处理条件中的关系 deviceManagement~authentication
        /*JSONArray serviceRelation=json.getJSONArray("serviceRelation");
        String REGEX = "(\\w+)~(\\w+)";
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(condition);

        while (matcher.find()) {
            String[] serviceArray=condition.split("~");
            String conditionConvert=serviceArray[1]+"~"+serviceArray[0];
            if(serviceList.contains(serviceArray[0])&&serviceList.contains(serviceArray[1])&&(serviceRelation.contains(condition)||serviceRelation.contains(conditionConvert))){
                condition = condition.replace(matcher.group(),"true");
            }else{
                condition = condition.replace(matcher.group(),"false");
            }
        }*/

        //处理条件中的场景 如*deviceAuth
        String REGEX = "\\*([\\w\\.]+)";
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(condition);
        while (matcher.find()) {
            if(sceneJson==null || !sceneJson.containsKey(matcher.group(1))){
                throw new CompileException("scene "+matcher.group(1)+" is not defined");
            }
            String status = sceneJson.getString(matcher.group(1));
            condition = condition.replace(matcher.group(),status);
        }

        //处理条件中的配置 如mysql.port
        REGEX = "#([\\w\\.\\-]+)";
        pattern = Pattern.compile(REGEX);
        matcher = pattern.matcher(condition);
        while (matcher.find()) {
            String configStr = "";
            try {
                configStr = getConfig(matcher.group(1));
            }catch (Exception e){
                throw new CompileException(matcher.group(1)+" can not match any config");
            }
            condition = condition.replace(matcher.group(),configStr);
        }

        //处理判断服务是否存在 @deviceManagement
        REGEX = "@([\\w\\-]+)";
        pattern = Pattern.compile(REGEX);
        matcher = pattern.matcher(condition);
        while (matcher.find()) {
            if(serviceList.contains(matcher.group(1))){
                condition = condition.replace(matcher.group(),"true");
            }else{
                condition = condition.replace(matcher.group(),"false");
            }
        }
        System.out.println(condition);
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        Object result = new Object();
        try{
            result = engine.eval(condition);
        }catch (ScriptException e) {
            e.printStackTrace();
        }
        if(result.toString().equals("true")){
            return true;
        }
        if(result.toString().equals("false")){
            return false;
        }
        return false;
    }

    @Override
    public void traversal(String libPath, String relativePath) throws Exception {
        String path = libPath + "/" + relativePath;
        File file = new File(path);
        File[] files=file.listFiles();
        for(File f:files){
            String newPath = relativePath+"/"+f.getName();
            if(f.isDirectory()){
                traversal(libPath,newPath);
            }else if(f.isFile()){
                String str= FileOperater.readFile(f.getPath());
                String finalStr=preprocess(str);
                String outPath = outputPath+"/"+newPath;
                FileOperater.writeFile(finalStr,outPath);
            }
        }
    }

}


