package com.pedalgaiden.application.port;

public interface CadenceSource extends AutoCloseable {
    float rpm();
    boolean connected();
    @Override void close();
}
