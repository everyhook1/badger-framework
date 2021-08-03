package org.badger.tcc.entity;

public @interface TxTcc {

    //最好设置一下,如果都设置的话,看着好看一些
    String gtxName();

    String btxName();

    TccStatus status();
}
