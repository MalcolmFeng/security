package com.inspur.health.filter;

import com.inspur.health.util.PropertiesUtils;
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

/**
 * 鉴权过滤器，查询token是否有访问此资源的权限；
 */
public class Filter2_TokenCheckFilter implements Filter {

    Logger logger = LoggerFactory.getLogger("simple");

    private String authUri = "/auth/authByJWT";  // 认证中心token校验
    private String unauthUri = "/error/authError";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        String token = (String) request.getAttribute("token");
        try{
            // 携带token访问认证服务器进行鉴权
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("uri",request.getRequestURI());
            formData.add("token",token);

            HttpHeaders authHeader = (HttpHeaders) request.getAttribute("authHeader");

            Map map = (Map)new RestTemplate().exchange(PropertiesUtils.getProp("auth.server") + authUri, HttpMethod.POST, new HttpEntity(formData, authHeader), Map.class, new Object[0]).getBody();
            if ((Integer)map.get("code") == 200){
                // 鉴权有效，允许访问资源
                filterChain.doFilter(request, servletResponse);
                return;
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
