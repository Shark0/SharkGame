package com.shark.game.entity.texasHoldEm;

import lombok.Data;

@Data
public class TexasHoldEmStartOperationResponseDO {
    private int seatId;
    private long minRaiseBet;
    private long maxRaiseBet;
    private boolean isCanRaise;
    private long callBet;
    private long operationTime;
}
