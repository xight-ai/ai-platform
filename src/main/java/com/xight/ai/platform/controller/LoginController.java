package com.xight.ai.platform.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xight.ai.platform.model.feishu.FeishuPrincipal;
import com.xight.ai.platform.model.feishu.FeishuUserInfo;
import com.xight.ai.platform.model.feishu.UserAccessTokenResponse;
import com.xight.ai.platform.service.FeishuHttpApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Collections;

@Controller
public class LoginController {

    @Autowired
    private FeishuHttpApi feishuHttpApi;

    @Value("${dify.appUrl}")
    private String difyAppUrl;

    @Value("${feishu.authorizeUrl}")
    private String authorizeUrl;

    //登录页
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("authorizeUrl", authorizeUrl);
        return "login"; // 返回登录页面
    }

    // 主页
    @GetMapping("/home")
    public String home(Model model, HttpSession session) {
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("userId", session.getAttribute("userId"));
        model.addAttribute("difyAppUrl", difyAppUrl);
        System.out.println(session.getAttribute("userId"));
        return "home_back"; // 返回登录成功后的页面
//        return "test"; // 返回登录成功后的页面
    }

    // 飞书认证后的回调接口
    @GetMapping("/callback")
    public String callback(@RequestParam(required = false) String code, String state, HttpSession session) {
        if (StringUtils.hasLength(code)) {
            //获取user_access_token
            UserAccessTokenResponse response = feishuHttpApi.getUserAccessToken(code);
            ObjectMapper mapper = new ObjectMapper();
            try {
                System.out.println(mapper.writeValueAsString(response));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            // 获取飞书用户信息
            FeishuUserInfo userInfo = feishuHttpApi.getUserInfo(response.getAccessToken()).getData();

            // 在spring security中进行认证
            FeishuPrincipal feishuPrincipal = new FeishuPrincipal();
            feishuPrincipal.setUserAccessToken(response.getAccessToken());
            feishuPrincipal.setUserId(userInfo.getOpenId());
            Authentication authentication = new UsernamePasswordAuthenticationToken(feishuPrincipal, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            session.setAttribute("userId", userInfo.getOpenId());
            session.setAttribute("username", userInfo.getName());

            System.out.println("code:" + code);
            System.out.println("state:" + state);
            return "redirect:/home";
        } else {
            return "redirect:/login";
        }

    }
}
