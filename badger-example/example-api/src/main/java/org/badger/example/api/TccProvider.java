package org.badger.example.api;

public interface TccProvider {

    void tryProvider(int a, int b);

    void confirmProvider(int a, int b);

    void cancelProvider(int a, int b);
}
