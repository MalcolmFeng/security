package com.inspur.health.filter;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

public class Filter2_TokenCheckFilter implements Filter {

    Logger logger = LoggerFactory.getLogger("simple");

    public static int EXPIRE_TIME= 60*60*1000;
    private String clientId = "net5ijy";  //客户端Id
    private String clientSecret = "123456"; // 客户端密钥
    private String authUrl = "http://localhost:7002/auth/authByJWT";  // 认证中心token校验
    private String unauthUri = "/error/authError";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper((HttpServletResponse) servletResponse);

        String token = (String) request.getAttribute("token");

        // 携带token访问认证服务器进行鉴权
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("uri",request.getRequestURI());
        formData.add("token",token);

        HttpHeaders headers = new HttpHeaders();
        String authorization = "Basic " + Base64.getUrlEncoder().encodeToString(( clientId+ ":" + clientSecret ).getBytes());
        headers.set("Authorization",authorization);
        headers.set("Content-Type","application/x-www-form-urlencoded");

        try{
            Map map = (Map)new RestTemplate().exchange(authUrl, HttpMethod.POST, new HttpEntity(formData, headers), Map.class, new Object[0]).getBody();
            if ((Integer)map.get("code") == 200){
                // 鉴权有效，允许访问资源
                filterChain.doFilter(request, servletResponse);
            }
        }catch (Exception e){
            logger.info(e.toString());
        }
        // 鉴权失败，转发到403页面
        request.getRequestDispatcher(unauthUri).forward(servletRequest,servletResponse);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }
}
