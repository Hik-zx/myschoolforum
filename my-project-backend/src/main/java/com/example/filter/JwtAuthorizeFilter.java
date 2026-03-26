package com.example.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 解析 JWT 令牌并将认证信息放入 Spring Security 的上下文
 * 每次请求时检查JWT，验证用户身份并设置认证信息，实现基于JWT的身份验证。
 */
@Component
public class JwtAuthorizeFilter extends OncePerRequestFilter {
    //继承OncePerRequestFilter表示每次请求过滤一次，用于快速编写JWT校验规则
    @Autowired
    JwtUtil jwtUtil;

    /**
     * 处理HTTP请求
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 获取请求的 URI
        String requestURI = request.getRequestURI();
        System.out.println(requestURI);
        // 如果请求的 URI 是登录请求，直接放行
//        if ("/api/auth/login".equals(requestURI)) {
//            filterChain.doFilter(request, response);
//            return;
//        }

        //从 HTTP 请求头中获取名为 "Authorize" 的头部信息的值
        String authorize = request.getHeader("Authorization");
        DecodedJWT jwt=jwtUtil.resloveJwt(authorize);
        if(jwt!=null){
            UserDetails userDetails= jwtUtil.toUser(jwt);
            //用于Spring Security的认证和授权机制中。
            //该对象表示一个已认证的用户。这里，密码设置为null，因为JWT认证机制不依赖密码的传递，而是依赖JWT的签名和有效期验证。
            // userDetails.getAuthorities()获取用户的权限信息。
            UsernamePasswordAuthenticationToken authenticationToken=new UsernamePasswordAuthenticationToken(userDetails,
                    null,userDetails.getAuthorities());
            //为认证令牌设置额外的详细信息，如远程地址和会话ID等。这些信息对于后续的授权和审计可能很有用。
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            //将创建的authenticationToken放入SecurityContextHolder的上下文中。
            // 这样，后续的Spring Security组件（如方法安全）就可以访问这个认证令牌，从而知道当前用户的身份和权限。
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            request.setAttribute("id",jwtUtil.toId(jwt));
        }
        //将请求传递给过滤器链中的下一个过滤器
        filterChain.doFilter(request,response);
    }
}
