package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;

import com.atguigu.gmall.product.test.TestFdfs;
import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sun.security.provider.ConfigFile;

import java.io.IOException;

@RequestMapping("/admin/product")
@RestController
@CrossOrigin
public class FileUploadController {
    @Value("${fileServer.url}")
    private String url;



    @SneakyThrows
    @RequestMapping("fileUpload")
    public Result fileUpload(@RequestParam("file") MultipartFile multipartFile ) throws IOException, MyException {

        String path = TestFdfs.class.getClassLoader().getResource("tracker.conf").getPath();

        ClientGlobal.init(path);

        // 连接tracker
        TrackerClient trackerClient = new TrackerClient();

        TrackerServer trackerServer = trackerClient.getConnection();

        System.out.println(trackerServer);

        // 连接storage
        StorageClient storageClient = new StorageClient(trackerServer,null);

        // 上传文件
        String filenameExtension = StringUtils.getFilenameExtension(multipartFile.getOriginalFilename());
        String[] jpgs = storageClient.upload_file(multipartFile.getBytes(), filenameExtension, null);

        // 返回url
        for (String jpg : jpgs) {
            url = url + "/"+jpg;
        }

        System.out.println(url);
        return Result.ok(url);
    }
}


