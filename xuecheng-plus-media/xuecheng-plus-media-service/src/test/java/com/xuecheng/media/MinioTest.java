package com.xuecheng.media;


import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.UploadObjectArgs;
import org.junit.jupiter.api.Test;

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
}
