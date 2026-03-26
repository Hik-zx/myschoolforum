package com.example.service.impl;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.Account;
import com.example.entity.ImageStore;
import com.example.mapper.AccountMapper;
import com.example.mapper.ImageStoreMapper;
import com.example.service.ImageService;
import com.example.util.Const;
import com.example.util.FlowUtils;
import io.minio.*;
import io.minio.errors.*;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 图片上传实现类
 */
@Service
public class ImageServiceImpl extends ServiceImpl<ImageStoreMapper, ImageStore> implements ImageService {
    @Autowired
    MinioClient minioClient;
    @Autowired
    AccountMapper accountMapper;
    @Autowired
    FlowUtils flowUtils;
    //创建日期格式
    SimpleDateFormat format=new SimpleDateFormat("yyyyMMdd");
    /**
     * 头像上传
     * @param multipartFile
     * @param id
     * @return
     */
    @Override
    public String uploadAvatar(MultipartFile multipartFile, int id) throws IOException {
        //生成图片名称
        String imageName= UUID.randomUUID().toString().replace("-"," ");
        //新的值取代旧的值（基础）
        imageName="/avatar/"+imageName;
        //填写上传的相关参数，使用PutObjectArgs的Builder来完成
        PutObjectArgs args = PutObjectArgs.builder()
                        .bucket("study")//桶的名称
                        .stream(multipartFile.getInputStream(), multipartFile.getSize(), -1)//指定了要上传的文件内容,大小
                        .object(imageName)//存入桶的图片名称
                        . build();
        try {
            //上传头像
            minioClient.putObject(args);
            String avatar = accountMapper.selectById(id).getAvatar();
            this.deleteOldAvatar(avatar);
            if(accountMapper.update( Wrappers.<Account>update().eq("id",id).set("avatar",imageName))>0){
                //上传成功
                return imageName;
            }else
                return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 上传图片
     * @param multipartFile
     * @param id
     * @return
     * @throws IOException
     */
    @Override
    public String uploadImage(MultipartFile multipartFile, int id) throws IOException {
        //判断是否恶意请求(1h内请求20次)
        String key= Const.FORUM_IMAGE_COUNTER+id;
        if(!flowUtils.limitPeriodCounterCheck(key,20,3600))
            return null;
        //生成图片名称
        String imageName= UUID.randomUUID().toString().replace("-"," ");
        Date date=new Date();
        imageName="/cache/"+format.format(date)+"/"+imageName;
        //填写上传的相关参数，使用PutObjectArgs的Builder来完成
        PutObjectArgs args = PutObjectArgs.builder()
                .bucket("study")//桶的名称
                .stream(multipartFile.getInputStream(), multipartFile.getSize(), -1)//指定了要上传的文件内容,大小
                .object(imageName)//存入桶的图片名称
                . build();
        try{
            //上传头像
            minioClient.putObject(args);
            if(this.save(new ImageStore(id,imageName,date))) {
                //上传成功
                return imageName;
            }else {
                return null;
            }
        }catch (Exception e){
            return null;
        }
    }

    /**
     * 获取头像
     * @param stream
     * @param image
     */
    @Override
    public void fetchImageFromMinio(OutputStream stream, String image) throws Exception {
        GetObjectArgs args= GetObjectArgs.builder().bucket("study").object(image).build();
        GetObjectResponse response = minioClient.getObject(args);
        //从 MinIO 获取到的对象响应 response 写入到输出流 stream 中，从而将图片数据写入到输出流中。
        IOUtils.copy(response,stream);
    }

    /**
     * 删除桶内数据
     * @param avatar
     * @throws Exception
     */
    private void deleteOldAvatar(String avatar) throws Exception {
        if(avatar == null || avatar.isEmpty()) return;
        RemoveObjectArgs remove = RemoveObjectArgs.builder()
                .bucket("study")
                .object(avatar)
                .build();
        minioClient.removeObject(remove);
    }
}
