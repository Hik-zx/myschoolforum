package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.ImageStore;
import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * 图片上传
 */
public interface ImageService extends IService<ImageStore> {
    /**
     * 头像上传
     * @param multipartFile
     * @param id
     * @return
     */
    String uploadAvatar(MultipartFile multipartFile,int id) throws IOException;

    /**
     * 上传图片
     * @param multipartFile
     * @param id
     * @return
     * @throws IOException
     */
    String uploadImage(MultipartFile multipartFile,int id) throws IOException;

    /**
     * 获取图片
     * @param stream
     * @param image
     * @throws Exception
     */
    void fetchImageFromMinio(OutputStream stream,String image) throws Exception;
}
