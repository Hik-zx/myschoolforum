package com.example.config;

import com.example.entity.Account;
import com.example.entity.vo.response.AuthorizeVo;
import com.example.filter.JwtAuthorizeFilter;
import com.example.result.ResultBean;
import com.example.service.AccountService;
import com.example.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Security相关配置
 */
@Configuration
public class SecurityConfiguration {
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    JwtAuthorizeFilter filter;
    @Autowired
    AccountService accountService;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                //用于配置请求的授权规则
                .authorizeHttpRequests(conf ->conf
                        .requestMatchers("/api/auth/**","/error","/image/**").permitAll()
                        .anyRequest().hasAnyRole("user")
                )
                //配置了基于表单的身份验证
                .formLogin(conf ->conf
                        .loginProcessingUrl("/api/auth/login")
                        .failureHandler(this::onAuthenticationFailure)
                        .successHandler(this::onAuthenticationSuccess)
                )
                //配置了退出登录的行为
                .logout(conf ->conf
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(this::onLogoutSuccess)
                )
                //配置异常处理
                .exceptionHandling(conf-> conf
                        //用于处理未经身份验证的用户尝试访问受保护资源的情况。
                        .authenticationEntryPoint(this::onUnauthorization)
                        .accessDeniedHandler(this::onAccessDeny)
                )
                .csrf(AbstractHttpConfigurer::disable)
                //配置了会话管理策略:无状态管理政策   即不会创建会话，每个请求都需要进行完整的身份验证。
                .sessionManagement(conf ->conf.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                //添加过滤器链,将自定义过滤器添加到它之前，可以在用户进行用户名密码认证之前执行自定义逻辑，例如处理基于 JSON Web Token (JWT) 的身份验证。
                //这样的配置允许你在 Spring Security 进行用户认证之前，对请求进行额外的处理，
                // 比如解析 JWT 并将用户信息设置到安全上下文中。这是一种常见的用法，用于实现基于令牌的身份验证机制。
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
//处理访问拒绝的情况(用户尝试访问他们没有权限的资源)
    private void onAccessDeny(HttpServletRequest request, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException {
        httpServletResponse.setContentType("application/json;charset=utf-8");
        httpServletResponse.getWriter().write(ResultBean.failure(401,e.getMessage()).asJsonString());
    }


    //因为前后端分离，所以不会处理页面的逻辑
    private void onLogoutSuccess(HttpServletRequest request,
                                 HttpServletResponse httpServletResponse,
                                 Authentication authentication) throws IOException {
        httpServletResponse.setContentType("application/json;charset=utf-8");
        PrintWriter writer = httpServletResponse.getWriter();
        String authorization = request.getHeader("Authorization");
        //退出登录后就应该让token失效
        if(jwtUtil.invalidateJwt(authorization)){
            writer.write(ResultBean.success().asJsonString());
        }else {
            writer.write(ResultBean.failure(400,"退出登录失败").asJsonString());
        }

    }
    //没有权限
    private void onUnauthorization(HttpServletRequest request,
                                   HttpServletResponse httpServletResponse,
                                   AuthenticationException e) throws IOException {
        httpServletResponse.setContentType("application/json;charset=utf-8");
        httpServletResponse.getWriter().write(ResultBean.failure(402,e.getMessage()).asJsonString());

    }
    //登录失败
    private void onAuthenticationFailure(HttpServletRequest request,
                                         HttpServletResponse httpServletResponse,
                                         AuthenticationException e) throws IOException , ServletException {
        httpServletResponse.setContentType("application/json;charset=utf-8");
        httpServletResponse.getWriter().write(ResultBean.failure(401,e.getMessage()).asJsonString());
    }
    //登录成功后
    private void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse httpServletResponse,
            Authentication authentication) throws IOException {
        httpServletResponse.setContentType("application/json;charset=utf-8");
        //从认证对象中获取用户信息，并将其转换为 User(userdetails) 对象以便后续使用。(从loadUserByUsername方法返回的)
        User user = (User) authentication.getPrincipal();
        Account account = accountService.findAccountByNameOrEmail(user.getUsername());//这里的getUsername()可能是username也可能是email
        String token = jwtUtil.creatJwt(user,account.getId(),account.getUsername());
        AuthorizeVo vo=new AuthorizeVo();
        BeanUtils.copyProperties(account,vo);
//        vo.setRole(account.getRole());
//        vo.setUsername(account.getUsername());
        vo.setExpire(jwtUtil.expireTime());

        vo.setToken(token);

        //写入响应体
        httpServletResponse.getWriter().write(ResultBean.success(vo).asJsonString());
    }
}
