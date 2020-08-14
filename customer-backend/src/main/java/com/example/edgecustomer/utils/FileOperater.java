package com.example.edgecustomer.utils;

import org.springframework.core.io.FileSystemResource;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileOperater {
    public static String readFile(String filePath){
        String str = "";
        try {
            FileSystemResource resource = new FileSystemResource(filePath);
            BufferedReader br = new BufferedReader(new FileReader(resource.getFile()));
            String line="";
            while((line = br.readLine()) != null){
                str+=line+"\n";
                //System.out.println(str);//此时str就保存了一行字符串
            }
            /*int byteread=0;
            while ((byteread=br.read())!=-1){
                str+=(char)byteread;
            }*/
            br.close();
        } catch (IOException e) {
            //todo loginfo
        }
        return str;
    }

    public static Boolean writeFile(String str,String filePath){
        try {
            File file=new File(filePath);
            if(!file.exists()) {
                file.getParentFile().mkdirs();
            }
            FileWriter fileWriter=new FileWriter(file);
            //FileSystemResource resource = new FileSystemResource(filePath);
            //FileWriter fileWriter=new FileWriter(resource.getFile());

            fileWriter.write(str);
            fileWriter.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static void deleteDir(String dirPath)
    {
        File file = new File(dirPath);
        if(file.isFile())
        {
            file.delete();
        }else
        {
            File[] files = file.listFiles();
            if(files == null)
            {
                file.delete();
            }else
            {
                for (int i = 0; i < files.length; i++)
                {
                    deleteDir(files[i].getAbsolutePath());
                }
                file.delete();
            }
        }
    }

    public static void copyDir(String oldPath, String newPath) throws IOException {
        File file = new File(oldPath);
        //文件名称列表
        String[] filePath = file.list();

        if (!(new File(newPath)).exists()) {
            (new File(newPath)).mkdirs();
        }

        for (int i = 0; i < filePath.length; i++) {
            if ((new File(oldPath + file.separator + filePath[i])).isDirectory()) {
                copyDir(oldPath  + file.separator  + filePath[i], newPath  + file.separator + filePath[i]);
            }

            if (new File(oldPath  + file.separator + filePath[i]).isFile()) {
                copyFile(oldPath + file.separator + filePath[i], newPath + file.separator + filePath[i]);
            }

        }
    }

    private static void copyFile(String oldPath, String newPath) throws IOException {
        File source = new File(oldPath);
        File dest = new File(newPath);
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            inputChannel.close();
            outputChannel.close();
        }
    }
    /*public static void copyFile(String oldPath, String newPath) throws IOException {
        File oldFile = new File(oldPath);
        File file = new File(newPath);
        FileInputStream in = new FileInputStream(oldFile);
        FileOutputStream out = new FileOutputStream(file);;

        //byte[] buffer=new byte[209715200];
        int fileLength = oldFile.length();
        byte[] buffer=new byte[fileLength];

        if(fileLength>209715200){
            System.out.println("file "+ oldFile.getName()+" is too large "+fileLength);
        }

        while((in.read(buffer)) != -1){
            out.write(buffer);
        }
    }*/

    public static void download(String filePath, HttpServletResponse res) throws IOException {
        // 发送给客户端的数据
        OutputStream outputStream = res.getOutputStream();
        byte[] buff = new byte[1024];
        BufferedInputStream bis = null;
        // 读取filename
        bis = new BufferedInputStream(new FileInputStream(new File(filePath)));
        int i = bis.read(buff);
        while (i != -1) {
            outputStream.write(buff, 0, buff.length);
            outputStream.flush();
            i = bis.read(buff);
        }
    }

    public static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

}
