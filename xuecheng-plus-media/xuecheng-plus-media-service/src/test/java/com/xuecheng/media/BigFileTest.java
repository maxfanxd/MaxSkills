package com.xuecheng.media;


import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.UploadObjectArgs;
import org.junit.jupiter.api.Test;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.apache.commons.codec.digest.DigestUtils.md5;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;

public class BigFileTest {

    @Test
    public void testChunk() throws IOException {
        // 测试分块
        File sourseFile = new File("F:\\test.mp4");
        // 分块文件存储路径
        String chunkFilePath = "F:\\upload\\chunk\\";
        // 分块文件的大小
        int chunkSize = 1024 * 1024 * 5;
        // 分块文件的个数
        int chunkNum = (int) Math.ceil(sourseFile.length() * 1.0 / chunkSize);
        // 使用流从源文件读数据，向分块文件中写数据
        RandomAccessFile raf_r = new RandomAccessFile(sourseFile, "r");
        // 缓冲区
        byte[] bytes = new byte[1024];
        for(int i = 0; i < chunkNum; i++) {
            File chunkFile = new File(chunkFilePath + i);
            // 写入流
            RandomAccessFile raf_rw = new RandomAccessFile(chunkFile, "rw");
            int len = -1;
            while((len=raf_r.read(bytes))!=-1){
                raf_rw.write(bytes,0,len);
                if(chunkFile.length()>=chunkSize){
                    break;
                }
            }
            raf_rw.close();
        }
        raf_r.close();
    }

    @Test void testMerge() throws IOException {
        // 测试合并
        // 块文件目录
        File chunkFolder = new File("F:\\upload\\chunk\\");
        // 合并后的文件
        File mergeFile = new File("F:\\upload\\merge\\test_merged.mp4");
        // 源文件
        File sourseFile = new File("F:\\test.mp4");

        // 取出所有分块文件
        File[] files = chunkFolder.listFiles();
        // 将数组转成list
        List<File> filesList = Arrays.asList(files);
        Collections.sort(filesList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                // 降序排列
                return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
            }
        });

        //向合并文件写的流
        RandomAccessFile raf_rw = new RandomAccessFile(mergeFile, "rw");
        // 缓冲区
        byte[] bytes = new byte[1024];

        // 遍历分块文件，向合并的文件写
        for(File file: filesList){
            // 读分块的流
            RandomAccessFile raf_r = new RandomAccessFile(file, "r");
            int len = -1;
            while((len=raf_r.read(bytes))!=-1){
                raf_rw.write(bytes,0,len);
            }
            raf_r.close();
        }
        raf_rw.close();

        // 进行校验
        FileInputStream fileInputStream_merge = new FileInputStream(mergeFile);
        String s1 = md5Hex(fileInputStream_merge);

        FileInputStream fileInputStream_source = new FileInputStream(sourseFile);
        String s2 = md5Hex(fileInputStream_source);

        if(s1.equals(s2)){
            System.out.println("文件合并成功");
        }
    }
}
