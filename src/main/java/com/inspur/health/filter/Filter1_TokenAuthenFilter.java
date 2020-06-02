package com.inspur.health.filter;

import com.inspur.health.util.PropertiesUtils;
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

/**
 * 认证过滤器，校验token
 */
public class Filter1_TokenAuthenFilter implements Filter {

    private String checkUri = "/oauth/check_token";  // 认证中心token校验

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
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
        String loginUrl = PropertiesUtils.getProp("auth.server") + "/oauth/authorize?response_type=code&client_id="+
                PropertiesUtils.getProp("client.id") + "&scope=all&redirect_uri="+ PropertiesUtils.getProp("app.server") +
                "/handler/code?target="+ request.getRequestURL();

        if (token == null){
            wrapper.sendRedirect(loginUrl);  // 重定向地址
            return;
        }

        try{
            /**
             * 2.访问认证中心校验token
             */
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("token",token);

            HttpHeaders authHeader = new HttpHeaders();
            String Authorization = "Basic " + Base64.getUrlEncoder().encodeToString(( PropertiesUtils.getProp("client.id")+ ":" + PropertiesUtils.getProp("client.secret")).getBytes());
            authHeader.set("Authorization",Authorization);
            authHeader.set("Content-Type","application/x-www-form-urlencoded");

            request.setAttribute("authHeader",authHeader);

            Map map = (Map)new RestTemplate().exchange(PropertiesUtils.getProp("auth.server")+checkUri, HttpMethod.POST, new HttpEntity(formData, authHeader), Map.class, new Object[0]).getBody();

            // token有效，允许访问资源
            request.setAttribute("token",token);
            filterChain.doFilter(request, servletResponse);
        }catch (Exception e){
            // token无效，跳转到登录页
            wrapper.sendRedirect(loginUrl);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }
}
