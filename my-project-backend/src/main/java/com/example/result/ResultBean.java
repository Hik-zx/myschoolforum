package com.example.result;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

public record ResultBean <T>(T data, String msg, int code){
    public static <T> ResultBean<T> success(T data){
        return new ResultBean<>(data,"请求成功",200);
    }
    public static <T> ResultBean<T> success(){
        return success(null);
    }
    public static <T> ResultBean<T> failure(int code,String message){
        return new ResultBean<>(null,message,code);
    }
    public String asJsonString(){
        return JSONObject.toJSONString(this, JSONWriter.Feature.WriteNulls);
    }
}
