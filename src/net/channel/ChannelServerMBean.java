package net.channel;

public interface ChannelServerMBean {
    void shutdown(int time);

    void shutdownWorld(int time);

    String getServerMessage();

    void setServerMessage(String newMessage);

    int getChannel();

    int getExpRate();

    void setExpRate(int expRate);

    int getConnectedClients();

    int getLoadedMaps();
}
