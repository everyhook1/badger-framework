package org.badger.example.api;

public interface TccBackend {

    void tryBackend(int a, int b);

    void confirmBackend(int a, int b);

    void cancelBackend(int a, int b);
}
