package com.valkyrie.nabeshimamac.twitter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterListener;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class MainActivity extends AppCompatActivity {

    private final String API_KEY = "4NEelxnwb12yhVfWbtIwPDEBO";
    private final String API_SECRET = "rxcragGH0czissQZOHAFAwXWtVj7YeAIlpeBgv3Cv1OZvc9ygv";
    private final String ACCESS_TOKEN = "1445763985-0g6aDcAZkRon6ymY4Gy7aWDFMwlIKfvVoqKxkdC";
    private final String ACCESS_TOKEN_SECRET = "0nAFew6XgzBvPgfZpm1ZDF9uVGgoWIcvaPUoXvhlHGrAJ";

    private final String PREF_FILE_NAME = "twitter_test";
    private final String PREF_TOKEN = "token";
    private final String PREF_SECRET = "secret";


    private AsyncTwitter mTwitter;
    private RequestToken mReqToken;

    private Handler mHandler;
    private EditText mText;
    private EditText mLogText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTwitter = new AsyncTwitterFactory().getInstance();
        mTwitter.addListener(mListener);
        mTwitter.setOAuthConsumer(API_KEY, API_SECRET);

        AccessToken token = getAccessToken();
        if (token == null) {
            mTwitter.getOAuthRequestTokenAsync("twittercallback://callback");
        } else {
            mTwitter.setOAuthAccessToken(token);
        }

        mText = (EditText) findViewById(R.id.editText1);
        mLogText = (EditText) findViewById(R.id.editText2);

        Button btnTweet = (Button) findViewById(R.id.button1);
        btnTweet.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String text = mText.getText().toString();
                mTwitter.updateStatus(text);
            }

        });
        Button btnSearch = (Button) findViewById(R.id.button2);
        btnSearch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String text = mText.getText().toString();
                Query query = new Query();
                query.setQuery(text);
                query.setCount(80);
                query.setResultType(Query.POPULAR);
                mTwitter.search(query);
            }

        });

        mHandler = new Handler();
    }

    private final TwitterListener mListener = new TwitterAdapter() {
        @Override
        public void gotOAuthRequestToken(RequestToken token) {
            mReqToken = token;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mReqToken.getAuthorizationURL()));
            startActivity(intent);
        }

        @Override
        public void gotOAuthAccessToken(AccessToken token) {
            SharedPreferences pref = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(PREF_TOKEN, token.getToken());
            editor.putString(PREF_SECRET, token.getTokenSecret());
            editor.commit();
            mTwitter.setOAuthAccessToken(new AccessToken(token.getToken(), token.getTokenSecret()));
        }

        @Override
        public void searched(QueryResult queryResult) {
            String log = "";
            for (Status status : queryResult.getTweets()) {
                log += status.getText() + "\n";
            }
            final String logText = log;
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mLogText.setText(logText);
                }

            });
        }

        @Override
        public void updatedStatus(Status status) {
            final String logText = "ID:" + status.getId() + "\n" + status.getText();
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mLogText.setText(logText);
                }

            });
        }


    };


    public AccessToken getAccessToken() {
        SharedPreferences pref = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        String token = pref.getString(PREF_TOKEN, null);
        String secret = pref.getString(PREF_SECRET, null);

//        String token = "1445763985-0g6aDcAZkRon6ymY4Gy7aWDFMwlIKfvVoqKxkdC";
//        String secret = "0nAFew6XgzBvPgfZpm1ZDF9uVGgoWIcvaPUoXvhlHGrAJ";

        if (token != null && secret != null) {
            return new AccessToken(token, secret);
        } else {
            return null;
        }

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


