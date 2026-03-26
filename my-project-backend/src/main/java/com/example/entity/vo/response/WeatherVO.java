package com.example.entity.vo.response;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 天气相关实体
 */
@Data
public class WeatherVO {
    //位置
    JSONObject location;
    //实时天气
    JSONObject now;
    //24小时预报
    JSONArray hourly;
}
