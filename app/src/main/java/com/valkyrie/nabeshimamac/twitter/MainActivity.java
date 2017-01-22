package com.valkyrie.nabeshimamac.twitter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterListener;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class MainActivity extends AppCompatActivity {

    private final String API_KEY = "4NEelxnwb12yhVfWbtIwPDEBO";
    private final String API_SECRET = "rxcragGH0czissQZOHAFAwXWtVj7YeAIlpeBgv3Cv1OZvc9ygv";

    private AsyncTwitter mTwitter;
    private RequestToken mReqToken;

    private final TwitterListener mListener = new TwitterAdapter() {
        @Override
        public void gotOAuthRequestToken(RequestToken token) {
            mReqToken = token;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mReqToken.getAuthorizationURL()));
            startActivity(intent);
        }

        @Override
        public void gotOAuthAccessToken(AccessToken token) {
            //token.getToken()とtoken.getTokenSecret()を保存する
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTwitter = new AsyncTwitterFactory().getInstance();
        mTwitter.addListener(mListener);
        mTwitter.setOAuthConsumer(API_KEY, API_SECRET);
        mTwitter.getOAuthRequestTokenAsync("twittercallback://callback");

    }

    @Override
    protected void onNewIntent(Intent intent) {
        //ブラウザからのコールバックで呼ばれる
        final Uri uri = intent.getData();
        final String verifier = uri.getQueryParameter("oauth_verifier");
        if (verifier != null) {
            mTwitter.getOAuthAccessTokenAsync(mReqToken, verifier);
        }
    }
}


