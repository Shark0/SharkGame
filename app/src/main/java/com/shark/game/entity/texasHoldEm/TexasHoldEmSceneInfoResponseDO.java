package com.shark.game.entity.texasHoldEm;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class TexasHoldEmSceneInfoResponseDO {
    private long roomBet;
    private List<Integer> publicCardList;
    private Map<Integer, TexasHoldEmSeatInfoResponseDO> seatIdSeatMap;
    private int smallBlindSeatId;
    private int bigBlindSeatId;
    private int currentOperationSeatId;
    private int sceneStatus;
}
