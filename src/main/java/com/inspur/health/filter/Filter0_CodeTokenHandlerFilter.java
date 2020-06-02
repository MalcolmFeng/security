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
 * 授权过滤器
 * 获取code，根据code换取token
 */
public class Filter0_CodeTokenHandlerFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper((HttpServletResponse) servletResponse);

        // 此过滤器只过滤code处理的回调
        if (!request.getRequestURI().contains("/handler/code")){
            filterChain.doFilter(request, servletResponse);
            return;
        }
        String code = request.getParameter("code");
        String target = request.getParameter("target");

        // 获取token
        String getTokenUrl = PropertiesUtils.getProp("auth.server") + "/oauth/token";
        int EXPIRE_TIME= 60*60*1000;
        String redirect_url = PropertiesUtils.getProp("app.server") + "/handler/code?target=";

        MultiValueMap<String, String> formData1 = new LinkedMultiValueMap<>();
        formData1.add("grant_type","authorization_code");
        formData1.add("scope","all");
        formData1.add("redirect_uri", redirect_url + target);
        formData1.add("code", code);

        HttpHeaders authHeader = new HttpHeaders();
        String Authorization = "Basic " + Base64.getUrlEncoder().encodeToString(( PropertiesUtils.getProp("client.id")+ ":" + PropertiesUtils.getProp("client.secret")).getBytes());
        authHeader.set("Authorization",Authorization);
        authHeader.set("Content-Type","application/x-www-form-urlencoded");

        try{

            Map map = new RestTemplate().exchange(getTokenUrl, HttpMethod.POST, new HttpEntity(formData1, authHeader), Map.class, new Object[0]).getBody();
            String token = (String)map.get("access_token");
            // 写入cookie
            Cookie cookie = new Cookie("token", token);
            cookie.setMaxAge(EXPIRE_TIME);// 设置为30min
            cookie.setPath("/");
            response.addCookie(cookie);

            response.sendRedirect(target);
        }catch (Exception e){
            System.out.println(e.toString());
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }
}
