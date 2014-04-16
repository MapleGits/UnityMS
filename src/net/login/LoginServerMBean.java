package net.login;

public interface LoginServerMBean {
    int getNumberOfSessions();

    int getPossibleLogins();

    int getLoginInterval();

    int getUserLimit();

    void setUserLimit(int newLimit);
}
