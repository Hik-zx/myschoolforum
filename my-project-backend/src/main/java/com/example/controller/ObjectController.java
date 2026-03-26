package com.example.controller;

import com.example.result.ResultBean;
import com.example.service.ImageService;
import io.minio.errors.ErrorResponseException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ObjectController {
    @Autowired
    ImageService service;

    /**
     * 获取头像
     * @param request
     * @param response
     * @throws Exception
     */
    @GetMapping("/image/**")
    public void imageFetch(HttpServletRequest request, HttpServletResponse response) throws Exception {
            this.fetchImage(request,response);
    }

    private void fetchImage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //去掉/images
        String imagePath=request.getServletPath().substring(7);
        ServletOutputStream outputStream = response.getOutputStream();
        if(imagePath.length()<=13){
            response.setStatus(404);
            outputStream.println(ResultBean.failure(404,"Not found").toString());
        }else {
            try{
                service.fetchImageFromMinio(outputStream,imagePath);
                response.setHeader("Cache-Control","max-age=2592000");
                response.setHeader("Content-Type","image/jpg");
            }catch (ErrorResponseException e){
                if(e.response().code()==404){
                    response.setStatus(404);
                    outputStream.println(ResultBean.failure(404,"Not found").toString());
                }
            }

        }
    }
}
