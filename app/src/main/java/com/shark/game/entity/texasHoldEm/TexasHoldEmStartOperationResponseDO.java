package com.shark.game.entity.texasHoldEm;

import lombok.Data;

@Data
public class TexasHoldEmStartOperationResponseDO {
    private int seatId;
    private long maxRaiseBet;
    private long minRaiseBet;
    private boolean isCanRaise;
    private long callBet;
    private long lastOperationTime;
}
