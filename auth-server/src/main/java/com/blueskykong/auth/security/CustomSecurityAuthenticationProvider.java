package com.blueskykong.auth.security;

import com.auth0.jwt.internal.org.apache.commons.lang3.StringUtils;
import com.blueskykong.auth.client.feign.UserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author keets
 * @date 2017/8/5
 */
@Component
public class CustomSecurityAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserClient userClient;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password;

        Map map;

        password = (String) authentication.getCredentials();
        //如果你是调用user服务，这边不用注掉
        //map = userClient.checkUsernameAndPassword(getUserServicePostObject(username, password, type));
        map = checkUsernameAndPassword(getUserServicePostObject(username, password));


        String userId = (String) map.get("userId");
        if (StringUtils.isBlank(userId)) {
            String errorCode = (String) map.get("code");
            throw new BadCredentialsException(errorCode);
        }
        CustomUserDetails customUserDetails = buildCustomUserDetails(username, password, userId);
        return new CustomAuthenticationToken(customUserDetails);
    }

    private CustomUserDetails buildCustomUserDetails(String username, String password, String userId) {
        CustomUserDetails customUserDetails = new CustomUserDetails.CustomUserDetailsBuilder()
                .withUserId(userId)
                .withPassword(password)
                .withUsername(username)
                .withClientId("for Security")
                .build();
        return customUserDetails;
    }

    private Map<String, String> getUserServicePostObject(String username, String password) {
        Map<String, String> requestParam = new HashMap<String, String>();
        requestParam.put("userName", username);
        requestParam.put("password", password);
        return requestParam;
    }

    //模拟调用user服务的方法
    private Map checkUsernameAndPassword(Map map) {

        //checkUsernameAndPassword
        Map ret = new HashMap();
        ret.put("userId", UUID.randomUUID().toString());

        return ret;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }

}