package com.xight.ai.platform.model.feishu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserAccessTokenRequest {

    @JsonProperty("grant_type")
    private String grantType = "authorization_code";

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("client_secret")
    private String clientSecret;

    private String code;

    @JsonProperty("redirect_uri")
    private String redirectUri;

    @JsonProperty("code_verifier")
    private String codeVerifier;

    private String scope;
}
