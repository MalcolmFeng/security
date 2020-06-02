package com.inspur.health.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping(value = "/handler")
public class HandlerController {

    public static int EXPIRE_TIME= 60*60*1000;
    public static String redirect_url = "http://localhost:9090/handler/code?target=";

    /**
     * 根据code，获取token
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/code")
    public void test(HttpServletRequest request, HttpServletResponse response){
        String code = request.getParameter("code");
        String target = request.getParameter("target");

        // 获取token
        String getTokenUrl = "http://localhost:7002/oauth/token";

        MultiValueMap<String, String> formData1 = new LinkedMultiValueMap<>();
        formData1.add("grant_type","authorization_code");
        formData1.add("scope","all");
        formData1.add("redirect_uri", redirect_url + target);
        formData1.add("code", code);

        HttpHeaders headers1 = new HttpHeaders();
        headers1.set("Authorization","Basic dGVuY2VudDoxMjM0NTY=");  // client-id: tencent

        try{

            Map map = (Map)new RestTemplate().exchange(getTokenUrl, HttpMethod.POST, new HttpEntity(formData1, headers1), Map.class, new Object[0]).getBody();
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
}
