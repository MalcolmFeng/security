package com.inspur.health.filter;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

public class Filter1_TokenAuthenFilter implements Filter {

    public static int EXPIRE_TIME= 60*60*1000;
    private String clientId = "net5ijy";  //客户端Id
    private String clientSecret = "123456"; // 客户端密钥
    private String checkUrl = "http://localhost:7002/oauth/check_token";  // 认证中心token校验
    private String loginUrl = "http://localhost:7002/oauth/authorize?response_type=code&client_id=tencent&scope=all&redirect_uri=http://localhost:9090/handler/code?target="; // 认证中心登录页

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper((HttpServletResponse) servletResponse);

        /**
         * 1.从参数、请求头、cookie中获取token，若没有，则重定向到 sso 获取token的登录页。
         */
        String token = request.getParameter("token");
        if (token == null){
            token = request.getHeader("token");
        }
        if (token == null){
            Cookie[]  cookies = request.getCookies();
            if (cookies != null){
                for(Cookie cookie : cookies){
                    if (cookie.getName().equals("token")){
                        // 在cookies中找到Token之后，结束遍历
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }
        if (token == null){
            wrapper.sendRedirect(loginUrl+ request.getRequestURL());  // 重定向地址
            return;
        }
        request.setAttribute("token",token); // 给下一个过滤器使用


        /**
         * 2.访问认证中心校验token
         */
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("token",token);

        HttpHeaders headers = new HttpHeaders();
        String authorization = "Basic " + Base64.getUrlEncoder().encodeToString(( clientId+ ":" + clientSecret ).getBytes());
        headers.set("Authorization",authorization);
        headers.set("Content-Type","application/x-www-form-urlencoded");
        try{
            Map map = (Map)new RestTemplate().exchange(checkUrl, HttpMethod.POST, new HttpEntity(formData, headers), Map.class, new Object[0]).getBody();
            // token有效，允许访问资源
            filterChain.doFilter(request, servletResponse);
        }catch (Exception e){
            // token无效，跳转到登录页
            wrapper.sendRedirect(loginUrl + request.getRequestURL());
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }
}
