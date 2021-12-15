package com.shark.game.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shark.game.R;
import com.shark.game.entity.seat.SeatDO;
import com.shark.game.entity.texasHoldEm.TexasHoldEmHandCardResponseDO;
import com.shark.game.entity.texasHoldEm.TexasHoldEmRoomInfoResponseDO;
import com.shark.game.entity.texasHoldEm.TexasHoldEmSeatOperationResponseDO;
import com.shark.game.entity.texasHoldEm.TexasHoldEmStartOperationResponseDO;
import com.shark.game.entity.texasHoldEm.TexasHoldEmWaitOperationResponseDO;
import com.shark.game.entity.texasHoldEm.TexasHoldEmWinPotBetResponseDO;
import com.shark.game.manager.ChannelManager;
import com.shark.game.service.TexasHoldemGameService;
import com.shark.game.service.TexasHoldemGameStatusServiceGrpc;
import com.shark.game.service.TexasHoldemOperationServiceGrpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.grpc.stub.StreamObserver;

public class TexasHoldEmActivity extends AppCompatActivity implements View.OnClickListener {

    private final int SEAT_STATUS_WAITING = 0, SEAT_STATUS_GAMING = 1;

    private final int ROOM_STATUS_WAITING = 0, ROOM_STATUS_PRE_FLOP = 1, ROOM_STATUS_FLOP = 2, ROOM_STATUS_TURN = 3, ROOM_STATUS_RIVER = 4,
            ROOM_STATUS_OPEN_CARD = 5, ROOM_STATUS_ALLOCATE_POT = 6;

    private final int OPERATION_EXIT = 0, OPERATION_CALL = 1, OPERATION_RAISE = 2, OPERATION_ALL_IN = 3,
            OPERATION_FOLD = 4, OPERATION_STAN_UP = 5, OPERATION_SIT_DOWN = 6;

    public static final int RESPONSE_STATUS_SIT_DOWN = 0, RESPONSE_STATUS_NO_SEAT = 1, RESPONSE_STATUS_SEAT_INFO = 2,
            RESPONSE_STATUS_ENTER_ROOM_INFO = 3, RESPONSE_STATUS_ROOM_INFO = 4, RESPONSE_STATUS_CHECK_SEAT_LIVE = 5,
            RESPONSE_STATUS_CHANGE_STATUS = 6, RESPONSE_STATUS_SEAT_CARD = 7, RESPONSE_STATUS_START_OPERATION = 8,
            RESPONSE_STATUS_WAIT_SEAT_OPERATION = 9, RESPONSE_STATUS_SEAT_OPERATION = 10, RESPONSE_STATUS_PUBLIC_CARD = 11,
            RESPONSE_STATUS_WIN_POT_BET = 12;

    public static String INTENT_TOKEN = "INTENT_TOKEN";

    private String token;

    private final Map<Integer, View> seatLayoutMap = new HashMap<>();
    private final Map<Integer, View> seatInfoLayoutMap = new HashMap<>();
    private final Map<Integer, TextView> seatNameTextViewMap = new HashMap<>();
    private final Map<Integer, TextView> seatCard1TextViewMap = new HashMap<>();
    private final Map<Integer, TextView> seatCard2TextViewMap = new HashMap<>();
    private final Map<Integer, TextView> seatCardTypeTextViewMap = new HashMap<>();
    private final Map<Integer, TextView> seatMoneyTextViewMap = new HashMap<>();
    private final Map<Integer, TextView> seatBetMoneyTextViewMap = new HashMap<>();
    private final Map<Integer, TextView> seatOperationTextViewMap = new HashMap<>();
    private final Map<Integer, View> seatEmptyTextViewMap = new HashMap<>();
    private final Map<Integer, View> seatProgressLayoutMap = new HashMap<>();
    private final Map<Integer, TextView> seatOperationSecondTextViewMap = new HashMap<>();
    private final Map<Integer, TextView> publicCardTextViewMap = new HashMap<>();

    private final List<Integer> publicCardList = new ArrayList<>();
    private final Map<Integer, SeatDO> seatIdSeatMap = new HashMap<>();
    private final Map<Integer, TexasHoldEmHandCardResponseDO> seatIdHandCardMap = new HashMap<>();
    private int sitDownSeatId = -1;
    private int smallBlindSeatId;
    private int roomStatus;
    private int bigBlindSeatId;
    private int currentOperationSeatId;
    private long callBet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texas_hold_em);
        bindContentView();
        token = getIntent().getStringExtra(INTENT_TOKEN);
        registerStatusResponse();
    }

    @Override
    public void onBackPressed() {
        TexasHoldemOperationServiceGrpc.TexasHoldemOperationServiceBlockingStub stub =
                TexasHoldemOperationServiceGrpc.newBlockingStub(ChannelManager.getInstance(getString(R.string.server_url)).getChannel());
        TexasHoldemGameService.TexasHoldemGameOperationRequest request =
                TexasHoldemGameService.TexasHoldemGameOperationRequest.newBuilder()
                        .setToken(token)
                        .setOperation(OPERATION_EXIT)
                        .setBet(0)
                        .build();
        stub.sendTexasHoldemGameOperation(request);
        super.onBackPressed();
    }

    private void bindContentView() {
        bindAllSeatLayout();
        bindAllSeatInfoLayout();
        bindAllSeatNameTextView();
        bindAllSeatCard1TextView();
        bindAllSeatCard2TextView();
        bindAllSeatCardTypeTextView();
        bindAllSeatMoneyTextView();
        bindAllSeatBetMoneyTextView();
        bindAllSeatOperationTextView();
        bindAllSeatProgressLayout();
        bindAllSeatOperationSecondTextView();
        bindAllSeatEmptyTextView();
        bindPublicCardTextView();
        bindFoldButton();
        bindCallButton();
        bindRaiseButton();
        bindBetRaiseSeekBar();
        bindStandUpButton();
        bindSitDownButton();
    }

    private void bindAllSeatLayout() {
        seatLayoutMap.put(0, findViewById(R.id.activityTexasHoldEm_seatLayout0));
        seatLayoutMap.put(1, findViewById(R.id.activityTexasHoldEm_seatLayout1));
        seatLayoutMap.put(2, findViewById(R.id.activityTexasHoldEm_seatLayout2));
        seatLayoutMap.put(3, findViewById(R.id.activityTexasHoldEm_seatLayout3));
        seatLayoutMap.put(4, findViewById(R.id.activityTexasHoldEm_seatLayout4));
        seatLayoutMap.put(5, findViewById(R.id.activityTexasHoldEm_seatLayout5));
        seatLayoutMap.put(6, findViewById(R.id.activityTexasHoldEm_seatLayout6));
        seatLayoutMap.put(7, findViewById(R.id.activityTexasHoldEm_seatLayout7));
        seatLayoutMap.put(8, findViewById(R.id.activityTexasHoldEm_seatLayout8));
        seatLayoutMap.put(9, findViewById(R.id.activityTexasHoldEm_seatLayout9));
        seatLayoutMap.put(10, findViewById(R.id.activityTexasHoldEm_seatLayout10));
        seatLayoutMap.put(11, findViewById(R.id.activityTexasHoldEm_seatLayout11));
    }

    private void bindAllSeatInfoLayout() {
        seatInfoLayoutMap.put(0, findViewById(R.id.activityTexasHoldEm_seatInfoLayout0));
        seatInfoLayoutMap.put(1, findViewById(R.id.activityTexasHoldEm_seatInfoLayout1));
        seatInfoLayoutMap.put(2, findViewById(R.id.activityTexasHoldEm_seatInfoLayout2));
        seatInfoLayoutMap.put(3, findViewById(R.id.activityTexasHoldEm_seatInfoLayout3));
        seatInfoLayoutMap.put(4, findViewById(R.id.activityTexasHoldEm_seatInfoLayout4));
        seatInfoLayoutMap.put(5, findViewById(R.id.activityTexasHoldEm_seatInfoLayout5));
        seatInfoLayoutMap.put(6, findViewById(R.id.activityTexasHoldEm_seatInfoLayout6));
        seatInfoLayoutMap.put(7, findViewById(R.id.activityTexasHoldEm_seatInfoLayout7));
        seatInfoLayoutMap.put(8, findViewById(R.id.activityTexasHoldEm_seatInfoLayout8));
        seatInfoLayoutMap.put(9, findViewById(R.id.activityTexasHoldEm_seatInfoLayout9));
        seatInfoLayoutMap.put(10, findViewById(R.id.activityTexasHoldEm_seatInfoLayout10));
        seatInfoLayoutMap.put(11, findViewById(R.id.activityTexasHoldEm_seatInfoLayout11));
    }

    private void bindAllSeatNameTextView() {
        seatNameTextViewMap.put(0, findViewById(R.id.activityTexasHoldEm_seatNameTextView0));
        seatNameTextViewMap.put(1, findViewById(R.id.activityTexasHoldEm_seatNameTextView1));
        seatNameTextViewMap.put(2, findViewById(R.id.activityTexasHoldEm_seatNameTextView2));
        seatNameTextViewMap.put(3, findViewById(R.id.activityTexasHoldEm_seatNameTextView3));
        seatNameTextViewMap.put(4, findViewById(R.id.activityTexasHoldEm_seatNameTextView4));
        seatNameTextViewMap.put(5, findViewById(R.id.activityTexasHoldEm_seatNameTextView5));
        seatNameTextViewMap.put(6, findViewById(R.id.activityTexasHoldEm_seatNameTextView6));
        seatNameTextViewMap.put(7, findViewById(R.id.activityTexasHoldEm_seatNameTextView7));
        seatNameTextViewMap.put(8, findViewById(R.id.activityTexasHoldEm_seatNameTextView8));
        seatNameTextViewMap.put(9, findViewById(R.id.activityTexasHoldEm_seatNameTextView9));
        seatNameTextViewMap.put(10, findViewById(R.id.activityTexasHoldEm_seatNameTextView10));
        seatNameTextViewMap.put(11, findViewById(R.id.activityTexasHoldEm_seatNameTextView11));
    }

    private void bindAllSeatCard1TextView() {
        seatCard1TextViewMap.put(0, findViewById(R.id.activityTexasHoldEm_seatCard1TextView0));
        seatCard1TextViewMap.put(1, findViewById(R.id.activityTexasHoldEm_seatCard1TextView1));
        seatCard1TextViewMap.put(2, findViewById(R.id.activityTexasHoldEm_seatCard1TextView2));
        seatCard1TextViewMap.put(3, findViewById(R.id.activityTexasHoldEm_seatCard1TextView3));
        seatCard1TextViewMap.put(4, findViewById(R.id.activityTexasHoldEm_seatCard1TextView4));
        seatCard1TextViewMap.put(5, findViewById(R.id.activityTexasHoldEm_seatCard1TextView5));
        seatCard1TextViewMap.put(6, findViewById(R.id.activityTexasHoldEm_seatCard1TextView6));
        seatCard1TextViewMap.put(7, findViewById(R.id.activityTexasHoldEm_seatCard1TextView7));
        seatCard1TextViewMap.put(8, findViewById(R.id.activityTexasHoldEm_seatCard1TextView8));
        seatCard1TextViewMap.put(9, findViewById(R.id.activityTexasHoldEm_seatCard1TextView9));
        seatCard1TextViewMap.put(10, findViewById(R.id.activityTexasHoldEm_seatCard1TextView10));
        seatCard1TextViewMap.put(11, findViewById(R.id.activityTexasHoldEm_seatCard1TextView11));
    }

    private void bindAllSeatCard2TextView() {
        seatCard2TextViewMap.put(0, findViewById(R.id.activityTexasHoldEm_seatCard2TextView0));
        seatCard2TextViewMap.put(1, findViewById(R.id.activityTexasHoldEm_seatCard2TextView1));
        seatCard2TextViewMap.put(2, findViewById(R.id.activityTexasHoldEm_seatCard2TextView2));
        seatCard2TextViewMap.put(3, findViewById(R.id.activityTexasHoldEm_seatCard2TextView3));
        seatCard2TextViewMap.put(4, findViewById(R.id.activityTexasHoldEm_seatCard2TextView4));
        seatCard2TextViewMap.put(5, findViewById(R.id.activityTexasHoldEm_seatCard2TextView5));
        seatCard2TextViewMap.put(6, findViewById(R.id.activityTexasHoldEm_seatCard2TextView6));
        seatCard2TextViewMap.put(7, findViewById(R.id.activityTexasHoldEm_seatCard2TextView7));
        seatCard2TextViewMap.put(8, findViewById(R.id.activityTexasHoldEm_seatCard2TextView8));
        seatCard2TextViewMap.put(9, findViewById(R.id.activityTexasHoldEm_seatCard2TextView9));
        seatCard2TextViewMap.put(10, findViewById(R.id.activityTexasHoldEm_seatCard2TextView10));
        seatCard2TextViewMap.put(11, findViewById(R.id.activityTexasHoldEm_seatCard2TextView11));
    }

    private void bindAllSeatCardTypeTextView() {
        seatCardTypeTextViewMap.put(0, findViewById(R.id.activityTexasHoldEm_seatCardTypeTextView0));
        seatCardTypeTextViewMap.put(1, findViewById(R.id.activityTexasHoldEm_seatCardTypeTextView1));
        seatCardTypeTextViewMap.put(2, findViewById(R.id.activityTexasHoldEm_seatCardTypeTextView2));
        seatCardTypeTextViewMap.put(3, findViewById(R.id.activityTexasHoldEm_seatCardTypeTextView3));
        seatCardTypeTextViewMap.put(4, findViewById(R.id.activityTexasHoldEm_seatCardTypeTextView4));
        seatCardTypeTextViewMap.put(5, findViewById(R.id.activityTexasHoldEm_seatCardTypeTextView5));
        seatCardTypeTextViewMap.put(6, findViewById(R.id.activityTexasHoldEm_seatCardTypeTextView6));
        seatCardTypeTextViewMap.put(7, findViewById(R.id.activityTexasHoldEm_seatCardTypeTextView7));
        seatCardTypeTextViewMap.put(8, findViewById(R.id.activityTexasHoldEm_seatCardTypeTextView8));
        seatCardTypeTextViewMap.put(9, findViewById(R.id.activityTexasHoldEm_seatCardTypeTextView9));
        seatCardTypeTextViewMap.put(10, findViewById(R.id.activityTexasHoldEm_seatCardTypeTextView10));
        seatCardTypeTextViewMap.put(11, findViewById(R.id.activityTexasHoldEm_seatCardTypeTextView11));
    }

    private void bindAllSeatMoneyTextView() {
        seatMoneyTextViewMap.put(0, findViewById(R.id.activityTexasHoldEm_seatMoneyTextView0));
        seatMoneyTextViewMap.put(1, findViewById(R.id.activityTexasHoldEm_seatMoneyTextView1));
        seatMoneyTextViewMap.put(2, findViewById(R.id.activityTexasHoldEm_seatMoneyTextView2));
        seatMoneyTextViewMap.put(3, findViewById(R.id.activityTexasHoldEm_seatMoneyTextView3));
        seatMoneyTextViewMap.put(4, findViewById(R.id.activityTexasHoldEm_seatMoneyTextView4));
        seatMoneyTextViewMap.put(5, findViewById(R.id.activityTexasHoldEm_seatMoneyTextView5));
        seatMoneyTextViewMap.put(6, findViewById(R.id.activityTexasHoldEm_seatMoneyTextView6));
        seatMoneyTextViewMap.put(7, findViewById(R.id.activityTexasHoldEm_seatMoneyTextView7));
        seatMoneyTextViewMap.put(8, findViewById(R.id.activityTexasHoldEm_seatMoneyTextView8));
        seatMoneyTextViewMap.put(9, findViewById(R.id.activityTexasHoldEm_seatMoneyTextView9));
        seatMoneyTextViewMap.put(10, findViewById(R.id.activityTexasHoldEm_seatMoneyTextView10));
        seatMoneyTextViewMap.put(11, findViewById(R.id.activityTexasHoldEm_seatMoneyTextView11));
    }

    private void bindAllSeatBetMoneyTextView() {
        seatBetMoneyTextViewMap.put(0, findViewById(R.id.activityTexasHoldEm_seatBetMoneyTextView0));
        seatBetMoneyTextViewMap.put(1, findViewById(R.id.activityTexasHoldEm_seatBetMoneyTextView1));
        seatBetMoneyTextViewMap.put(2, findViewById(R.id.activityTexasHoldEm_seatBetMoneyTextView2));
        seatBetMoneyTextViewMap.put(3, findViewById(R.id.activityTexasHoldEm_seatBetMoneyTextView3));
        seatBetMoneyTextViewMap.put(4, findViewById(R.id.activityTexasHoldEm_seatBetMoneyTextView4));
        seatBetMoneyTextViewMap.put(5, findViewById(R.id.activityTexasHoldEm_seatBetMoneyTextView5));
        seatBetMoneyTextViewMap.put(6, findViewById(R.id.activityTexasHoldEm_seatBetMoneyTextView6));
        seatBetMoneyTextViewMap.put(7, findViewById(R.id.activityTexasHoldEm_seatBetMoneyTextView7));
        seatBetMoneyTextViewMap.put(8, findViewById(R.id.activityTexasHoldEm_seatBetMoneyTextView8));
        seatBetMoneyTextViewMap.put(9, findViewById(R.id.activityTexasHoldEm_seatBetMoneyTextView9));
        seatBetMoneyTextViewMap.put(10, findViewById(R.id.activityTexasHoldEm_seatBetMoneyTextView10));
        seatBetMoneyTextViewMap.put(11, findViewById(R.id.activityTexasHoldEm_seatBetMoneyTextView11));
    }

    private void bindAllSeatOperationTextView() {
        seatOperationTextViewMap.put(0, findViewById(R.id.activityTexasHoldEm_seatOperationTextView0));
        seatOperationTextViewMap.put(1, findViewById(R.id.activityTexasHoldEm_seatOperationTextView1));
        seatOperationTextViewMap.put(2, findViewById(R.id.activityTexasHoldEm_seatOperationTextView2));
        seatOperationTextViewMap.put(3, findViewById(R.id.activityTexasHoldEm_seatOperationTextView3));
        seatOperationTextViewMap.put(4, findViewById(R.id.activityTexasHoldEm_seatOperationTextView4));
        seatOperationTextViewMap.put(5, findViewById(R.id.activityTexasHoldEm_seatOperationTextView5));
        seatOperationTextViewMap.put(6, findViewById(R.id.activityTexasHoldEm_seatOperationTextView6));
        seatOperationTextViewMap.put(7, findViewById(R.id.activityTexasHoldEm_seatOperationTextView7));
        seatOperationTextViewMap.put(8, findViewById(R.id.activityTexasHoldEm_seatOperationTextView8));
        seatOperationTextViewMap.put(9, findViewById(R.id.activityTexasHoldEm_seatOperationTextView9));
        seatOperationTextViewMap.put(10, findViewById(R.id.activityTexasHoldEm_seatOperationTextView10));
        seatOperationTextViewMap.put(11, findViewById(R.id.activityTexasHoldEm_seatOperationTextView11));
    }

    private void bindAllSeatProgressLayout() {
        seatProgressLayoutMap.put(0, findViewById(R.id.activityTexasHoldEm_seatProgressLayout0));
        seatProgressLayoutMap.put(1, findViewById(R.id.activityTexasHoldEm_seatProgressLayout1));
        seatProgressLayoutMap.put(2, findViewById(R.id.activityTexasHoldEm_seatProgressLayout2));
        seatProgressLayoutMap.put(3, findViewById(R.id.activityTexasHoldEm_seatProgressLayout3));
        seatProgressLayoutMap.put(4, findViewById(R.id.activityTexasHoldEm_seatProgressLayout4));
        seatProgressLayoutMap.put(5, findViewById(R.id.activityTexasHoldEm_seatProgressLayout5));
        seatProgressLayoutMap.put(6, findViewById(R.id.activityTexasHoldEm_seatProgressLayout6));
        seatProgressLayoutMap.put(7, findViewById(R.id.activityTexasHoldEm_seatProgressLayout7));
        seatProgressLayoutMap.put(8, findViewById(R.id.activityTexasHoldEm_seatProgressLayout8));
        seatProgressLayoutMap.put(9, findViewById(R.id.activityTexasHoldEm_seatProgressLayout9));
        seatProgressLayoutMap.put(10, findViewById(R.id.activityTexasHoldEm_seatProgressLayout10));
        seatProgressLayoutMap.put(11, findViewById(R.id.activityTexasHoldEm_seatProgressLayout11));
    }

    private void bindAllSeatOperationSecondTextView() {
        seatOperationSecondTextViewMap.put(0, findViewById(R.id.activityTexasHoldEm_seatOperationSecondTextView0));
        seatOperationSecondTextViewMap.put(1, findViewById(R.id.activityTexasHoldEm_seatOperationSecondTextView1));
        seatOperationSecondTextViewMap.put(2, findViewById(R.id.activityTexasHoldEm_seatOperationSecondTextView2));
        seatOperationSecondTextViewMap.put(3, findViewById(R.id.activityTexasHoldEm_seatOperationSecondTextView3));
        seatOperationSecondTextViewMap.put(4, findViewById(R.id.activityTexasHoldEm_seatOperationSecondTextView4));
        seatOperationSecondTextViewMap.put(5, findViewById(R.id.activityTexasHoldEm_seatOperationSecondTextView5));
        seatOperationSecondTextViewMap.put(6, findViewById(R.id.activityTexasHoldEm_seatOperationSecondTextView6));
        seatOperationSecondTextViewMap.put(7, findViewById(R.id.activityTexasHoldEm_seatOperationSecondTextView7));
        seatOperationSecondTextViewMap.put(8, findViewById(R.id.activityTexasHoldEm_seatOperationSecondTextView8));
        seatOperationSecondTextViewMap.put(9, findViewById(R.id.activityTexasHoldEm_seatOperationSecondTextView9));
        seatOperationSecondTextViewMap.put(10, findViewById(R.id.activityTexasHoldEm_seatOperationSecondTextView10));
        seatOperationSecondTextViewMap.put(11, findViewById(R.id.activityTexasHoldEm_seatOperationSecondTextView11));
    }

    private void bindAllSeatEmptyTextView() {
        seatEmptyTextViewMap.put(0, findViewById(R.id.activityTexasHoldEm_seatEmptyTextView0));
        seatEmptyTextViewMap.put(1, findViewById(R.id.activityTexasHoldEm_seatEmptyTextView1));
        seatEmptyTextViewMap.put(2, findViewById(R.id.activityTexasHoldEm_seatEmptyTextView2));
        seatEmptyTextViewMap.put(3, findViewById(R.id.activityTexasHoldEm_seatEmptyTextView3));
        seatEmptyTextViewMap.put(4, findViewById(R.id.activityTexasHoldEm_seatEmptyTextView4));
        seatEmptyTextViewMap.put(5, findViewById(R.id.activityTexasHoldEm_seatEmptyTextView5));
        seatEmptyTextViewMap.put(6, findViewById(R.id.activityTexasHoldEm_seatEmptyTextView6));
        seatEmptyTextViewMap.put(7, findViewById(R.id.activityTexasHoldEm_seatEmptyTextView7));
        seatEmptyTextViewMap.put(8, findViewById(R.id.activityTexasHoldEm_seatEmptyTextView8));
        seatEmptyTextViewMap.put(9, findViewById(R.id.activityTexasHoldEm_seatEmptyTextView9));
        seatEmptyTextViewMap.put(10, findViewById(R.id.activityTexasHoldEm_seatEmptyTextView10));
        seatEmptyTextViewMap.put(11, findViewById(R.id.activityTexasHoldEm_seatEmptyTextView11));
    }

    private void bindPublicCardTextView() {
        publicCardTextViewMap.put(0, findViewById(R.id.activityTexasHoldEm_publicCardTextView0));
        publicCardTextViewMap.put(1, findViewById(R.id.activityTexasHoldEm_publicCardTextView1));
        publicCardTextViewMap.put(2, findViewById(R.id.activityTexasHoldEm_publicCardTextView2));
        publicCardTextViewMap.put(3, findViewById(R.id.activityTexasHoldEm_publicCardTextView3));
        publicCardTextViewMap.put(4, findViewById(R.id.activityTexasHoldEm_publicCardTextView4));
    }

    private void bindFoldButton() {
        findViewById(R.id.activityTexasHoldEm_foldButton).setOnClickListener(this);
    }

    private void bindCallButton() {
        findViewById(R.id.activityTexasHoldEm_callButton).setOnClickListener(this);
    }

    private void bindRaiseButton() {
        findViewById(R.id.activityTexasHoldEm_raiseButton).setOnClickListener(this);
    }

    private void bindBetRaiseSeekBar() {
        SeekBar seekBar = findViewById(R.id.activityTexasHoldEm_raiseBetSeekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Object minRaiseBetObject = seekBar.getTag();
                int minRaiseBet;
                if(minRaiseBetObject != null) {
                    minRaiseBet = (Integer) minRaiseBetObject;
                    if(minRaiseBet > progress) {
                        progress = minRaiseBet;
                        seekBar.setProgress(progress);
                    }
                }

                TextView raiseMoneyTextView = findViewById(R.id.activityTexasHoldEm_raiseMoneyTextView);
                raiseMoneyTextView.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    private void bindStandUpButton() {
        findViewById(R.id.activityTexasHoldEm_standUpButton).setOnClickListener(this);
    }

    private void bindSitDownButton() {
        findViewById(R.id.activityTexasHoldEm_sitDownButton).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activityTexasHoldEm_foldButton:
                onFoldButtonClick();
                break;
            case R.id.activityTexasHoldEm_callButton:
                onCallButtonClick();
                break;
            case R.id.activityTexasHoldEm_raiseButton:
                onRaiseButtonClick();
                break;
            case R.id.activityTexasHoldEm_standUpButton:
                onStandUpButtonClick();
                break;
            case R.id.activityTexasHoldEm_sitDownButton:
                onSitDownButtonClick();
                break;
        }
    }

    private void onFoldButtonClick() {
        if(sitDownSeatId != currentOperationSeatId) {
            return;
        }

        TexasHoldemOperationServiceGrpc.TexasHoldemOperationServiceBlockingStub stub =
                TexasHoldemOperationServiceGrpc.newBlockingStub(ChannelManager.getInstance(getString(R.string.server_url)).getChannel());
        TexasHoldemGameService.TexasHoldemGameOperationRequest request =
                TexasHoldemGameService.TexasHoldemGameOperationRequest.newBuilder()
                        .setToken(token)
                        .setOperation(OPERATION_FOLD)
                        .setBet(0)
                        .build();
        stub.sendTexasHoldemGameOperation(request);
    }

    private void onCallButtonClick() {
        if(sitDownSeatId != currentOperationSeatId) {
            return;
        }

        Button callButton = findViewById(R.id.activityTexasHoldEm_callButton);
        callButton.setText("跟注");

        TexasHoldemOperationServiceGrpc.TexasHoldemOperationServiceBlockingStub stub =
                TexasHoldemOperationServiceGrpc.newBlockingStub(ChannelManager.getInstance(getString(R.string.server_url)).getChannel());
        TexasHoldemGameService.TexasHoldemGameOperationRequest request =
                TexasHoldemGameService.TexasHoldemGameOperationRequest.newBuilder()
                        .setToken(token)
                        .setOperation(OPERATION_CALL)
                        .setBet(callBet)
                        .build();
        stub.sendTexasHoldemGameOperation(request);
    }

    private void onRaiseButtonClick() {
        if(sitDownSeatId != currentOperationSeatId) {
            return;
        }

        Button callButton = findViewById(R.id.activityTexasHoldEm_callButton);
        callButton.setText("跟注");

        SeekBar raiseSeekBar = findViewById(R.id.activityTexasHoldEm_raiseBetSeekBar);
        long raiseMoney = raiseSeekBar.getProgress();
        TexasHoldemOperationServiceGrpc.TexasHoldemOperationServiceBlockingStub stub =
                TexasHoldemOperationServiceGrpc.newBlockingStub(ChannelManager.getInstance(getString(R.string.server_url)).getChannel());
        TexasHoldemGameService.TexasHoldemGameOperationRequest request =
                TexasHoldemGameService.TexasHoldemGameOperationRequest.newBuilder()
                        .setToken(token)
                        .setOperation(OPERATION_RAISE)
                        .setBet(raiseMoney)
                        .build();
        stub.sendTexasHoldemGameOperation(request);
    }


    private void onStandUpButtonClick() {
        if(sitDownSeatId == currentOperationSeatId) {
            Button callButton = findViewById(R.id.activityTexasHoldEm_callButton);
            callButton.setText("跟注");
        }

        TexasHoldemOperationServiceGrpc.TexasHoldemOperationServiceBlockingStub stub =
                TexasHoldemOperationServiceGrpc.newBlockingStub(ChannelManager.getInstance(getString(R.string.server_url)).getChannel());
        TexasHoldemGameService.TexasHoldemGameOperationRequest request =
                TexasHoldemGameService.TexasHoldemGameOperationRequest.newBuilder()
                        .setToken(token)
                        .setOperation(OPERATION_STAN_UP)
                        .setBet(0)
                        .build();
        stub.sendTexasHoldemGameOperation(request);
        seatLayoutMap.get(sitDownSeatId).setBackgroundResource(R.drawable.seat_bg);
        sitDownSeatId = -1;
        findViewById(R.id.activityTexasHoldEm_operationLayout).setVisibility(View.GONE);
        findViewById(R.id.activityTexasHoldEm_sitDownButton).setVisibility(View.VISIBLE);
    }

    private void onSitDownButtonClick() {
        TexasHoldemOperationServiceGrpc.TexasHoldemOperationServiceBlockingStub stub =
                TexasHoldemOperationServiceGrpc.newBlockingStub(ChannelManager.getInstance(getString(R.string.server_url)).getChannel());
        TexasHoldemGameService.TexasHoldemGameOperationRequest request =
                TexasHoldemGameService.TexasHoldemGameOperationRequest.newBuilder()
                        .setToken(token)
                        .setOperation(OPERATION_SIT_DOWN)
                        .setBet(0)
                        .build();
        stub.sendTexasHoldemGameOperation(request);
    }

    private void registerStatusResponse() {
        Log.i("TexasHoldEmActivity", "registerStatusResponse");
        TexasHoldemGameStatusServiceGrpc.TexasHoldemGameStatusServiceStub stub
                = TexasHoldemGameStatusServiceGrpc.newStub(ChannelManager.getInstance(getString(R.string.server_url)).getChannel());
        TexasHoldemGameService.TexasHoldemGameStatusRequest request =
                TexasHoldemGameService.TexasHoldemGameStatusRequest.newBuilder().setToken(token).build();
        stub.registerTexasHoldemGameStatus(request, new StreamObserver<TexasHoldemGameService.TexasHoldemGameStatusResponse>() {
            @Override
            public void onNext(TexasHoldemGameService.TexasHoldemGameStatusResponse response) {
                runOnUiThread(() -> {
                    int status = response.getStatus();
                    String message = response.getMessage();
                    switch (status) {
                        case RESPONSE_STATUS_SIT_DOWN:
                            sitDown(Integer.valueOf(message));
                            break;
                        case RESPONSE_STATUS_NO_SEAT:
                            showNoSeatToast();
                            break;
                        case RESPONSE_STATUS_ENTER_ROOM_INFO:
                            TexasHoldEmRoomInfoResponseDO enterRoomInfo = new Gson().fromJson(message, TexasHoldEmRoomInfoResponseDO.class);
                            layoutRoomInfo(enterRoomInfo);
                            showEnterRoomToast(enterRoomInfo.getRoomStatus());
                            break;
                        case RESPONSE_STATUS_SEAT_INFO:
                            SeatDO seatDO = new Gson().fromJson(message, SeatDO.class);
                            layoutSeatInfo(seatDO.getId(), seatDO);
                            break;
                        case RESPONSE_STATUS_ROOM_INFO:
                            Log.i("TexasHoldEmActivity", "registerStatusResponse(): message = " + message);
                            layoutRoomInfo(new Gson().fromJson(message, TexasHoldEmRoomInfoResponseDO.class));
                            break;
                        case RESPONSE_STATUS_CHANGE_STATUS:
                            TexasHoldEmActivity.this.roomStatus = Integer.valueOf(message);
                            invisibleAllSeatProgressLayout();
                            changeStatus(TexasHoldEmActivity.this.roomStatus);
                            Toast.makeText(TexasHoldEmActivity.this, generateRoomStatusText(roomStatus), Toast.LENGTH_SHORT).show();
                            break;
                        case RESPONSE_STATUS_SEAT_CARD:
                            TexasHoldEmActivity.this.seatIdHandCardMap.clear();
                            TexasHoldEmActivity.this.seatIdHandCardMap.putAll(new Gson().fromJson(message, new TypeToken<Map<Integer, TexasHoldEmHandCardResponseDO>>() {}.getType()));
                            layoutAllSeatCard();
                            break;
                        case RESPONSE_STATUS_START_OPERATION:
                            startOperation(new Gson().fromJson(message, TexasHoldEmStartOperationResponseDO.class));
                            break;
                        case RESPONSE_STATUS_WAIT_SEAT_OPERATION:
                            layoutWaitSeatOperation(new Gson().fromJson(message, TexasHoldEmWaitOperationResponseDO.class));
                            break;
                        case RESPONSE_STATUS_SEAT_OPERATION:
                            layoutSeatOperation(new Gson().fromJson(message, TexasHoldEmSeatOperationResponseDO.class));
                            break;
                        case RESPONSE_STATUS_PUBLIC_CARD:
                            publicCardList.addAll(new Gson().fromJson(message, new TypeToken<List<Integer>>(){}.getType()));
                            layoutPublicCardList();
                            break;
                        case RESPONSE_STATUS_WIN_POT_BET:
                            displayWinPotBet(new Gson().fromJson(message, new TypeToken<List<TexasHoldEmWinPotBetResponseDO>>(){}.getType()), 0);
                            break;

                    }
                });
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {}
        });
    }

    private void sitDown(int seatId) {
        this.sitDownSeatId = seatId;
        seatLayoutMap.get(seatId).setBackgroundResource(R.drawable.sit_seat_bg);
        findViewById(R.id.activityTexasHoldEm_operationLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.activityTexasHoldEm_sitDownButton).setVisibility(View.GONE);
    }

    private void showNoSeatToast() {
        Toast.makeText(this, "目前沒有座位，請先觀看其他人遊戲操作，等有座位時再坐下", Toast.LENGTH_SHORT).show();
    }


    private void showEnterRoomToast(int roomStatus) {
        String message;
        switch (roomStatus) {
            case ROOM_STATUS_WAITING:
                message = "遊戲即將開始，請稍後";
                break;
            default:
                message = "目前遊戲進行中，請等下局";
                break;
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    private void layoutSeatInfo(int seatId, SeatDO seatDO) {
        if (seatDO == null) {
            seatInfoLayoutMap.get(seatId).setVisibility(View.INVISIBLE);
            seatEmptyTextViewMap.get(seatId).setVisibility(View.VISIBLE);
        } else {
            seatInfoLayoutMap.get(seatId).setVisibility(View.VISIBLE);
            seatEmptyTextViewMap.get(seatId).setVisibility(View.INVISIBLE);

            if (seatId == smallBlindSeatId) {
                seatNameTextViewMap.get(seatId).setText("小盲");
            } else if (seatId == bigBlindSeatId) {
                seatNameTextViewMap.get(seatId).setText("大盲");
            } else {
                seatNameTextViewMap.get(seatId).setText(seatDO.getPlayerName());
            }

            if(seatDO.getStatus() == SEAT_STATUS_WAITING || seatDO.getId() != seatId) {
                seatCard1TextViewMap.get(seatId).setText("牌1");
                seatCard2TextViewMap.get(seatId).setText("牌2");
                seatCardTypeTextViewMap.get(seatId).setText("牌型");
            }

            seatMoneyTextViewMap.get(seatId).setText("籌碼: " + seatDO.getMoney());
            seatBetMoneyTextViewMap.get(seatId).setText("押注: " + seatDO.getRoundBet());

            if (seatDO.getStatus() == SEAT_STATUS_WAITING) {
                seatOperationTextViewMap.get(seatId).setText("等下局");
            } else if (seatDO.getStatus() == SEAT_STATUS_GAMING) {
                int operation = seatDO.getOperation();
                seatOperationTextViewMap.get(seatId).setText(generateOperationText(operation));
            }
        }
    }

    private void layoutRoomInfo(TexasHoldEmRoomInfoResponseDO texasHoldEmRoomInfoResponseDO) {
        Log.i("TexasHoldEmActivity", "layoutRoomInfo()");
        smallBlindSeatId = texasHoldEmRoomInfoResponseDO.getSmallBlindSeatId();
        bigBlindSeatId = texasHoldEmRoomInfoResponseDO.getBigBlindSeatId();
        currentOperationSeatId = texasHoldEmRoomInfoResponseDO.getCurrentOperationSeatId();
        roomStatus = texasHoldEmRoomInfoResponseDO.getRoomStatus();

        TextView roomBetTextView = findViewById(R.id.activityTexasHoldEm_roomGameBetTextView);
        if(roomStatus == ROOM_STATUS_WAITING) {
            roomBetTextView.setText("");
        } else {
            roomBetTextView.setText("押注: " + texasHoldEmRoomInfoResponseDO.getRoomBet());
        }

        publicCardList.clear();
        publicCardList.addAll(texasHoldEmRoomInfoResponseDO.getPublicCardList());
        layoutPublicCardList();

        seatIdSeatMap.clear();
        seatIdSeatMap.putAll(texasHoldEmRoomInfoResponseDO.getSeatIdSeatMap());
        for(int seatId = 0; seatId < 12; seatId ++) {
            layoutSeatInfo(seatId, seatIdSeatMap.get(seatId));
        }
    }

    private void invisibleAllSeatProgressLayout() {
        for(int i = 0; i < 12; i ++) {
            seatProgressLayoutMap.get(i).setVisibility(View.INVISIBLE);
        }
    }

    private void changeStatus(int roomStatus) {
        for(int seatId = 0; seatId < 12; seatId ++) {
            switch (roomStatus) {
                case ROOM_STATUS_WAITING:
                    seatLayoutMap.get(seatId).setBackgroundResource((this.sitDownSeatId == seatId) ? R.drawable.sitdown_seat_bg: R.drawable.seat_bg);
                    break;
                default:
                    seatBetMoneyTextViewMap.get(seatId).setText("押注: 0");
                    break;
            }
        }
    }

    private void layoutPublicCardList() {
        Log.i("TexasHoldEmActivity", "layoutPublicCards()");
        for (int i = 0; i < 5; i++) {
            TextView publicCardTextView = publicCardTextViewMap.get(i);
            if (publicCardList.size() > i) {
                int card = publicCardList.get(i);
                String cardColor = generateCardColorText(card);
                String cardNumber = generateCardNumberText(card);
                publicCardTextView.setText(cardColor + cardNumber);
            } else {
                publicCardTextView.setText("牌" + (i + 1));
            }
        }
    }

    private void displayWinPotBet(List<TexasHoldEmWinPotBetResponseDO> winPotBetResponseDOList, int index) {
        if(index >= winPotBetResponseDOList.size()) {
            return;
        }
        TexasHoldEmWinPotBetResponseDO winPotBetResponseDO = winPotBetResponseDOList.get(index);
        for(Integer seatId: winPotBetResponseDO.getWinnerSeatIdList()) {
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            seatDO.setMoney(seatDO.getMoney() + winPotBetResponseDO.getWinBet());
            seatLayoutMap.get(seatId).setBackgroundResource(R.drawable.winner_seat_bg);
            seatMoneyTextViewMap.get(seatId).setText("籌碼: " + seatDO.getMoney());
            seatBetMoneyTextViewMap.get(seatId).setText("+ " + winPotBetResponseDO.getWinBet());
        }
        int nextIndex = index + 1;
        if(nextIndex >= winPotBetResponseDOList.size()) {
            return;
        }
        new Handler().postDelayed(() -> displayWinPotBet(winPotBetResponseDOList, nextIndex), 1000);
    }

    private void layoutAllSeatCard() {
        for (int seatId = 0; seatId < 12; seatId++) {
            TexasHoldEmHandCardResponseDO handCard = seatIdHandCardMap.get(seatId);
            if (handCard == null) {
                seatCard1TextViewMap.get(seatId).setText("牌1");
                seatCard2TextViewMap.get(seatId).setText("牌2");
                seatCardTypeTextViewMap.get(seatId).setText("牌型");
            } else {
                List<Integer> cardList = handCard.getCardList();
                if (cardList.size() > 0) {
                    int card1 = cardList.get(0);
                    seatCard1TextViewMap.get(seatId).setText(generateCardColorText(card1) + generateCardNumberText(card1));
                }
                if (cardList.size() > 1) {
                    int card2 = cardList.get(1);
                    seatCard2TextViewMap.get(seatId).setText(generateCardColorText(card2) + generateCardNumberText(card2));
                }
                seatCardTypeTextViewMap.get(seatId).setText(generateCardTypeText(handCard.getCardType()));
            }
        }
    }



    private void startOperation(TexasHoldEmStartOperationResponseDO startOperationResponseDO) {
        Log.i("TexasHoldEmActivity", "startOperation(): startOperationResponseDO = " + new Gson().toJson(startOperationResponseDO));
        this.currentOperationSeatId = startOperationResponseDO.getSeatId();
        seatProgressLayoutMap.get(currentOperationSeatId).setVisibility(View.VISIBLE);

        long lastOperationTime = startOperationResponseDO.getLastOperationTime();
        TextView operationSecondTextView = seatOperationSecondTextViewMap.get(currentOperationSeatId);
        operationSecondTextView.setText(String.valueOf(lastOperationTime / 1000));

        if (this.sitDownSeatId == currentOperationSeatId) {
            this.callBet = startOperationResponseDO.getCallBet();
            Button callButton = findViewById(R.id.activityTexasHoldEm_callButton);
            if (startOperationResponseDO.getCallBet() == 0) {
                callButton.setText("跟注");
            } else {
                callButton.setText("跟注: " + startOperationResponseDO.getCallBet());
            }

            View raiseButton = findViewById(R.id.activityTexasHoldEm_raiseButton);
            SeekBar raiseMoneySeekBar = findViewById(R.id.activityTexasHoldEm_raiseBetSeekBar);
            TextView raiseMoneyTextView = findViewById(R.id.activityTexasHoldEm_raiseMoneyTextView);
            if (startOperationResponseDO.isCanRaise()) {
                raiseButton.setClickable(true);
                int minRaiseBet = Long.valueOf(startOperationResponseDO.getMinRaiseBet()).intValue();
                raiseMoneySeekBar.setMax(Long.valueOf(startOperationResponseDO.getMaxRaiseBet()).intValue());
                raiseMoneySeekBar.setTag(minRaiseBet);
                raiseMoneySeekBar.setProgress(minRaiseBet);
                raiseMoneySeekBar.setVisibility(View.VISIBLE);
                raiseMoneyTextView.setVisibility(View.VISIBLE);
            } else {
                raiseButton.setClickable(false);
                raiseMoneySeekBar.setVisibility(View.GONE);
                raiseMoneyTextView.setVisibility(View.GONE);
            }
        }
    }


    private void layoutWaitSeatOperation(TexasHoldEmWaitOperationResponseDO waitOperationResponseDO) {
        long lastOperationTime = waitOperationResponseDO.getLastOperationTime();
        TextView operationSecondTextView = seatOperationSecondTextViewMap.get(waitOperationResponseDO.getSeatId());
        operationSecondTextView.setText(String.valueOf(lastOperationTime / 1000));
    }

    private void layoutSeatOperation(TexasHoldEmSeatOperationResponseDO seatOperationResponseDO) {
        int operationSeatId = seatOperationResponseDO.getSeatId();
        seatProgressLayoutMap.get(operationSeatId).setVisibility(View.GONE);
        seatOperationTextViewMap.get(operationSeatId).setText(generateOperationText(seatOperationResponseDO.getOperation()));

        if(seatOperationResponseDO.getOperation() == OPERATION_CALL || seatOperationResponseDO.getOperation() == OPERATION_RAISE) {
            seatMoneyTextViewMap.get(operationSeatId).setText("籌碼: " + seatOperationResponseDO.getMoney());
            seatBetMoneyTextViewMap.get(operationSeatId).setText("押注: " + seatOperationResponseDO.getBet());
        }

        TextView roomGameBetTextView = findViewById(R.id.activityTexasHoldEm_roomGameBetTextView);
        roomGameBetTextView.setText("押注: " + seatOperationResponseDO.getRoomGameBet());
    }

    private String generateRoomStatusText(int roomStatus) {
        switch (roomStatus) {
            case ROOM_STATUS_WAITING:
                return "遊戲即將開始，請稍後";
            case ROOM_STATUS_PRE_FLOP:
                return "遊戲開始，發牌";
            case ROOM_STATUS_FLOP:
                return "翻牌";
            case ROOM_STATUS_TURN:
                return "轉牌";
            case ROOM_STATUS_RIVER:
                return "河牌";
            case ROOM_STATUS_OPEN_CARD:
                return "開牌";
            case ROOM_STATUS_ALLOCATE_POT:
                return "分配賭池";
            default:
                return "";
        }
    }

    private String generateOperationText(int operation) {
        switch (operation) {
            case OPERATION_EXIT:
                return "離開";
            case OPERATION_FOLD:
                return "棄牌";
            case OPERATION_CALL:
                return "跟注";
            case OPERATION_RAISE:
                return "加注";
            case OPERATION_ALL_IN:
                return "ALL IN";
            case OPERATION_STAN_UP:
                return "站起";
        }
        return "";
    }

    private String generateCardColorText(int card) {
        int color = card / 13;
        return new String[]{"♣", "♦", "♥", "♠"}[color];
    }

    private String generateCardNumberText(int card) {
        int number = card % 13;
        return new String[]{"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"}[number];
    }

    private String generateCardTypeText(int cardType) {
        return new String[]{"高牌", "一對", "二對", "三張", "順子", "同花", "葫蘆", "鐵支", "同花順", "皇家"}[cardType];
    }
}
