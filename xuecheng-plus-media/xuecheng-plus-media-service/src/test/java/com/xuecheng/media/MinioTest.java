package com.xuecheng.media;


import io.minio.*;
import io.minio.errors.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MinioTest {
    MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.101.65:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();

    @Test
    public void test_upload() throws Exception {
        minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket("testbucket")
                        .object("test//01yolo.txt")
                        .filename("C:\\Users\\MaxFan\\Desktop\\银行.txt")
                        .build());
    }

    @Test
    public void test_delete() throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket("testbucket")
                        .object("test/01yolo.txt")
                        .build());
    }

    // 将分块文件上传到minio
    @Test
    public void uploadChunk() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        for(int i = 0; i <= 82; i++){
            minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket("testbucket")
                        .object("chunk/"+i)
                        .filename("F:\\upload\\chunk\\"+i)
                        .build());
            System.out.println("上传成功分块"+i+"成功");
        }
    }

    // 调用minio接口合并分块
    @Test
    public void testMerge() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
//        List<ComposeSource> sources = new ArrayList<>();
//        for(int i = 0; i <= 410; i++) {
//            ComposeSource composeSource = ComposeSource.builder().bucket("testBucket").object("chunk/" + i).build();
//            sources.add(composeSource);
//        }

        List<ComposeSource> sources = Stream.iterate(0, i -> ++i).limit(82).map(i -> ComposeSource.builder().bucket("testbucket").object("chunk/" + i).build()).collect(Collectors.toList());

        // minio 默认的分块文件大小是5M

        // 制定合并后的objectName信息
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket("testbucket")
                .object("merge01.mp4")
                .sources(sources)
                .build();
        // SDK自带，合并文件
        minioClient.composeObject(composeObjectArgs);
    }

    // 批量清理分块文件
}
