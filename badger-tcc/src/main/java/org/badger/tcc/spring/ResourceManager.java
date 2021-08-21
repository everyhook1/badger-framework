package org.badger.tcc.spring;

import org.badger.tcc.entity.ParticipantDTO;

import java.lang.reflect.InvocationTargetException;

public interface ResourceManager {

    Object commit(ParticipantDTO participantDTO) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;

    Object rollback(ParticipantDTO participantDTO) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;
}
