package com.example.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.xml.crypto.Data;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {
    @Value("${spring.security.jwt.key}")
    String key;
    @Value("${spring.security.jwt.expire}")
    int expire;
    @Resource
    StringRedisTemplate stringRedis;

    //判断jwt是否加入黑名单(验证传入的 JWT 是否有效并将其标记为失效状态)
    public boolean invalidateJwt(String headertoken){
        String token=this.convertToken(headertoken);
        if(token==null)
            return false;
        Algorithm algorithm=Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try{
            //验证给定的 JWT 是否有效。
            DecodedJWT verify = jwtVerifier.verify(token);
            String id = verify.getId();
            return deleteToken(id,verify.getExpiresAt());
        }catch (JWTVerificationException e){
            return false;
        }
    }
    //加入黑名单（让token失效）
    private boolean deleteToken(String uuid,Date time){
        if(this.isInvalidToken(uuid))
            return false;
        Date now=new Date();
        //确保过期时间不会为负数
        long expire = Math.max(time.getTime() - now.getTime(), 0);
        stringRedis.opsForValue().set(Const.JWT_BLACK_LIST+uuid,"",expire, TimeUnit.MILLISECONDS);
        return true;
    }
    //判断redis中黑名单中是否存在该token（失效token）
     private boolean isInvalidToken(String uuid){
     return Boolean.TRUE.equals(stringRedis.hasKey(Const.JWT_BLACK_LIST + uuid));
    }
    //创造jwt
    public String creatJwt(UserDetails details,int id,String username){
        //加密算法
        Algorithm algorithm=Algorithm.HMAC256(key);
        Date date=this.expireTime();
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("name", username)  //配置JWT自定义信息
                .withClaim("id", id)
                .withClaim("authorities",
                details.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())// 用户权限列表
                .withExpiresAt((Date) date)  //设置过期时间
                .withIssuedAt(new Date())    //设置创建创建时间
                .sign(algorithm);   //最终签名

    }

    //解析jwt
    public DecodedJWT resloveJwt(String headertoken){
        String token=this.convertToken(headertoken);
        if(token==null)
            return null;
        Algorithm algorithm=Algorithm.HMAC256(key);
        //创建了一个 JWTVerifier 实例(创建验证器)
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try{
            //验证给定的 JWT 是否有效。(解析)
            DecodedJWT verify = jwtVerifier.verify(token);
            //如果token失效，返回null
            if(isInvalidToken(verify.getId()))
                return null;
            //获取过期时间
            Date expiresAt = verify.getExpiresAt();
            //判断jwt是否过期
            return new Date().after(expiresAt)?null:verify;
        }catch (JWTVerificationException e){
            return null;
        }
    }
    //生成过期时间
    public Date expireTime(){
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.HOUR,expire *24);
        return  calendar.getTime();
    }

    public String convertToken(String token){
        if(token == null || !token.startsWith("Bearer "))
            return null;
        return token.substring(7);
    }
    //解析用户
    public UserDetails toUser(DecodedJWT jwt){
        Map<String, Claim> claims = jwt.getClaims();
        return User.withUsername(claims.get("name").asString())
                .password("*******")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();

    }
    //解析id
    public Integer toId(DecodedJWT jwt){
        Map<String, Claim> claims = jwt.getClaims();
        return claims.get("id").asInt();
    }
}
