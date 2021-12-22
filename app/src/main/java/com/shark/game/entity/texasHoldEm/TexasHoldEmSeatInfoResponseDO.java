package com.shark.game.entity.texasHoldEm;

import lombok.Data;

@Data
public class TexasHoldEmSeatInfoResponseDO {
    private String name;
    private int id;
    private int status;
    private int action;
    private long money;
    private long roundBet;
}
