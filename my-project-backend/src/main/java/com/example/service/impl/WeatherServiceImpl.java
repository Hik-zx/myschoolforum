package com.example.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.entity.vo.response.WeatherVO;
import com.example.service.WeatherService;
import com.example.util.Const;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

@Service
public class WeatherServiceImpl implements WeatherService {
    @Autowired
    private RestTemplate rest;
    @Resource
    StringRedisTemplate template;
    @Value("${spring.weather.key}")
    String key;
    /**
     * 通过经纬度获取天气信息
     * @param longitude
     * @param latitude
     * @return
     */
    public WeatherVO fetchWeather(double longitude, double latitude){
        return fetchFromCache(longitude, latitude);
    }

    private WeatherVO fetchFromCache(double longitude, double latitude){
        //处理API返回的压缩数据,转成JSONObject
        JSONObject geo = this.decompressStringToJson(rest.getForObject(
                "https://geoapi.qweather.com/v2/city/lookup?location="+longitude+","+latitude+"&key="+key, byte[].class));
        if(geo == null) return null;
        JSONArray locationArray = geo.getJSONArray("location");
        if (locationArray != null && locationArray.size() > 0) {
            //只获取第一个地址
            JSONObject location = locationArray.getJSONObject(0);
            //查出对应id以遍后续查找城市天气
            int id = location.getInteger("id");
            String key = Const.FORUM_WEATHER_CACHE +id;
            String cache = template.opsForValue().get(key);
            if(cache != null)
                //将JSON字符串转换为WeatherVO对象并返回。
                return JSONObject.parseObject(cache).to(WeatherVO.class);
            WeatherVO vo = this.fetchFromAPI(id, location);
            if(vo == null) return null;
            //存入redis缓存
            template.opsForValue().set(key, JSONObject.toJSONString(vo), 1, TimeUnit.HOURS);
            return vo;
        } else {
            return null;
        }


    }

    private WeatherVO fetchFromAPI(int id, JSONObject location){
        WeatherVO vo=new WeatherVO();
        vo.setLocation(location);
        //获取现在的天气
        JSONObject now = this.decompressStringToJson(rest.getForObject(
                "https://devapi.qweather.com/v7/weather/now?location="+id+"&key="+key, byte[].class));
        if(now == null) return null;
        vo.setNow(now.getJSONObject("now"));
        //获取未来24小时天气
        JSONObject hourly = this.decompressStringToJson(rest.getForObject(
                "https://devapi.qweather.com/v7/weather/24h?location="+id+"&key="+key, byte[].class));
        if(hourly == null) return null;
        //24小时天气我只获取前5个小时的天气
        vo.setHourly(new JSONArray(hourly.getJSONArray("hourly").stream().limit(5).toList()));
        return vo;
    }

    /**
     * 解压gzip数据
     * @param data
     * @return
     */
    private JSONObject decompressStringToJson(byte[] data){
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        try{
            GZIPInputStream gzip=new GZIPInputStream(new ByteArrayInputStream(data));
            byte[] buffer=new byte[1024];
            int read;
            while ((read= gzip.read(buffer))!=-1)
                stream.write(buffer,0,read);
            gzip.close();
            stream.close();

            return JSONObject.parseObject(stream.toString());
        }catch (IOException e){
            return null;
        }
    }
}
