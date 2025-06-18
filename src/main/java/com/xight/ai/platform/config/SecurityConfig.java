package com.xight.ai.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .antMatchers("/login").permitAll() // 允许匿名访问登录页
                        .antMatchers("/callback").permitAll() // 允许访问飞书回调页
                        .antMatchers("/api/queryByWikiIds").permitAll() // 允许访问知识库检索
                        .antMatchers("/api/queryFromRagFlow").permitAll() // 允许访问知识库检索
                        .antMatchers("/api/redis").permitAll() // 允许访问知识库检索
                        .antMatchers("/images/**", "/css/**", "/js/**").permitAll()
                        .antMatchers("/test/**").permitAll() // 允许访问测试接口
                        .anyRequest().authenticated() // 其他请求需要认证
                )
                .formLogin(form -> form
                        .loginPage("/login").permitAll() // 自定义登录页面
                        .defaultSuccessUrl("/home", true) // 登录成功后的默认跳转页面
//                        .defaultSuccessUrl("/home", true) // 登录成功后的默认跳转页面
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // 自定义登出 URL
                        .invalidateHttpSession(true)
                        .logoutSuccessUrl("/login") // 登出成功后的跳转页面
                );
        return http.build();
    }
}