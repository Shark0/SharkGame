package com.shark.game.manager;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class ChannelManager {

    private static ChannelManager instance;

    private ManagedChannel channel;

    private ChannelManager(String gameServerUrl) {
        channel = ManagedChannelBuilder.forTarget(gameServerUrl).usePlaintext().build();
    }

    public ManagedChannel getChannel() {
        return channel;
    }

    public void shutDown() {
        channel.shutdownNow();
    }

    public static ChannelManager getInstance(String gameServerUrl) {
        if(instance == null) {
            instance = new ChannelManager(gameServerUrl);
        }
        return instance;
    }
}
