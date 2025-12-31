package com.briup.pai.test;

import com.briup.pai.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {
    public static void main(String[] args) {
        // 测试token生成和解析
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", 1);
        String token = JwtUtil.generateJwt(claims);
        System.out.println("生成的token: " + token);
        
        try {
            Claims parsedClaims = JwtUtil.parseJWT(token);
            System.out.println("解析出的id: " + parsedClaims.get("id"));
            System.out.println("id的类型: " + parsedClaims.get("id").getClass());
            
            Integer userId = JwtUtil.getUserId(token);
            System.out.println("获取到的userId: " + userId);
        } catch (Exception e) {
            System.out.println("解析token时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}