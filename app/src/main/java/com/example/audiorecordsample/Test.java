package com.example.audiorecordsample;


import com.example.audiorecordsample.util.Constants;
import com.google.api.client.auth.oauth.AbstractOAuthGetToken;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Test{
    List<String> scopes = Arrays.asList(
            "https://www.googleapis.com/auth/cloud-platform"
    );


    protected AuthorizationCodeFlow newFlow() throws IOException {
        return new GoogleAuthorizationCodeFlow.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                BuildConfig.OAUTH_CLIENT_ID,
                BuildConfig.OAUTH_CLIENT_SECRET,
                scopes).setDataStoreFactory(
                MemoryDataStoreFactory.getDefaultInstance()).build();
    }

}
