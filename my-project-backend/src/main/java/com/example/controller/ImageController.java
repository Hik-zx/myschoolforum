package com.example.controller;

import com.example.result.ResultBean;
import com.example.service.ImageService;
import io.minio.MinioClient;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 图片上传
 */
@RestController
@RequestMapping("/api/image")
public class ImageController {
    @Autowired
    private ImageService imageService;

    /**
     * 上传头像
     * @param multipartFile
     * @param id
     * @return
     * @throws IOException
     */
    @PostMapping("/avatar")
    public ResultBean<String> uploadAvatar(@RequestParam("file")MultipartFile multipartFile,
                                           @RequestAttribute("id") int id) throws IOException {
        if(multipartFile.getSize()>1024 *200)
            return ResultBean.failure(400,"头像不能大于200KB");
        String message = imageService.uploadAvatar(multipartFile, id);
        if(message !=null){
            return ResultBean.success(message);
        }
        return ResultBean.failure(400,"头像上传失败，请联系管理员");
    }

    /**
     * 上传图片(发帖子缓存图片)
     * @param multipartFile
     * @param id
     * @return
     * @throws IOException
     */
    @PostMapping("/cache")
    public ResultBean<String> uploadImage(@RequestParam("file")MultipartFile multipartFile,
                                           @RequestAttribute("id") int id,
                                           HttpServletResponse reponse) throws IOException {
        System.out.println(multipartFile.getSize()/(1024*1024)+"MB");
        if(multipartFile.getSize()>1024 * 1024*10 ){
            reponse.setStatus(400);
            return ResultBean.failure(400,"图片不能大于20MB");
        }

        String message = imageService.uploadImage(multipartFile, id);
        if(message !=null){
            return ResultBean.success(message);
        }
        reponse.setStatus(400);
        return ResultBean.failure(400,"图片上传失败，请联系管理员");
    }
}
