package com.shark.game.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.shark.game.R;
import com.shark.game.manager.ChannelManager;
import com.shark.game.service.EnterGameRoomServiceGrpc;
import com.shark.game.service.EnterGameRoomServiceOuterClass;
import com.shark.game.service.LoginServiceGrpc;
import com.shark.game.service.LoginServiceOuterClass;
import com.shark.game.util.StringUtil;

import io.grpc.Channel;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindContentView();
    }

    @Override
    protected void onDestroy() {
        ChannelManager.getInstance(getString(R.string.server_url)).shutDown();
        super.onDestroy();
    }

    private void bindContentView() {
        bindLoginButton();
    }

    private void bindLoginButton() {
        findViewById(R.id.activityMain_loginButton).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activityMain_loginButton:
                onLoginButtonClick();
                break;
        }
    }

    private void onLoginButtonClick() {
        Log.i("MainActivity", "onLoginButtonClick");
        EditText editText = findViewById(R.id.activityMain_nameEditText);
        String name = editText.getText().toString();
        if(StringUtil.isEmpty(name)) {
            Toast.makeText(this, "請輸入暱稱", Toast.LENGTH_SHORT).show();
            return;
        }
        String token = requestLoginService(name);
        startTexasHoldEmActivity(token);
    }

    private String requestLoginService(String name) {
        Channel channel = ChannelManager.getInstance(getString(R.string.server_url)).getChannel();
        LoginServiceGrpc.LoginServiceBlockingStub loginServiceBlockingStub = LoginServiceGrpc.newBlockingStub(channel);
        LoginServiceOuterClass.LoginRequest loginRequest =
                LoginServiceOuterClass.LoginRequest.newBuilder().setPlayerName(name).build();
        LoginServiceOuterClass.LoginResponse loginResponse =
                loginServiceBlockingStub.start(loginRequest);
        String token = loginResponse.getToken();

        EnterGameRoomServiceOuterClass.EnterGameRoomRequest enterGameRequest =
                EnterGameRoomServiceOuterClass.EnterGameRoomRequest.newBuilder()
                        .setToken(token).setRoomType(3).build();
        EnterGameRoomServiceGrpc.EnterGameRoomServiceBlockingStub enterGameRoomServiceBlockingStub = EnterGameRoomServiceGrpc.newBlockingStub(channel);
        enterGameRoomServiceBlockingStub.start(enterGameRequest);


        return token;
    }

    private void startTexasHoldEmActivity(String token) {
        Intent intent = new Intent(this, TexasHoldEmActivity.class);
        intent.putExtra(TexasHoldEmActivity.INTENT_TOKEN, token);
        startActivity(intent);
    }
}