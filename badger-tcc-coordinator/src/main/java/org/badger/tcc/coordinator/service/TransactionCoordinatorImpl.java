/**
 * @(#)TransactionCoordinatorImpl.java, 8月 11, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.tcc.coordinator.service;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.badger.common.api.RpcProvider;
import org.badger.tcc.entity.ParticipantDTO;
import org.badger.tcc.entity.TransactionDTO;
import org.badger.tcc.spring.TransactionCoordinator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author liubin01
 */
@Slf4j
@RpcProvider
@Service
public class TransactionCoordinatorImpl implements TransactionCoordinator {

    @Autowired
    private NamedParameterJdbcTemplate db;

    @Override
    public TransactionDTO getTransaction(String gxid) {
        log.info("TransactionCoordinatorImpl getTransaction {}", gxid);
        List<TransactionDTO> transactionDTOS = db.queryForList("SELECT * FROM `transaction` WHERE gxid=:gxid",
                ImmutableMap.of("gxid", gxid), TransactionDTO.class);
        if (transactionDTOS.size() == 0) {
            return null;
        }
        TransactionDTO transactionDTO = transactionDTOS.get(0);
        List<ParticipantDTO> participantDTOS = db.queryForList("SELECT * FROM `participant` WHERE gxid=:gxid",
                ImmutableMap.of("gxid", gxid), ParticipantDTO.class);
        transactionDTO.setParticipantDTOS(participantDTOS);
        return transactionDTO;

    }

    @Override
    public void update(TransactionDTO transactionDTO) {
        log.info("TransactionCoordinatorImpl transaction {}", transactionDTO);
        List<TransactionDTO> transactionDTOS = db.queryForList("SELECT * FROM `transaction` WHERE gxid=:gxid",
                ImmutableMap.of("gxid", transactionDTO.getGxid()), TransactionDTO.class);
        if (transactionDTOS.size() == 0) {
            db.update("INSERT INTO `transaction` SET gxid=:gxid,rxid=`rxid`,status=`status`", ImmutableMap.of(
                    "gxid", transactionDTO.getGxid(), "rxid", transactionDTO.getRxid(), "status", transactionDTO.getStatus()));
        } else {
            db.update("UPDATE `transaction` SET  status=`status` WHERE gxid=:gxid", ImmutableMap.of(
                    "status", transactionDTO.getStatus(), "gxid", transactionDTO.getGxid()));
        }
        if (!CollectionUtils.isEmpty(transactionDTO.getParticipantDTOS())) {
            transactionDTO.getParticipantDTOS().forEach(this::update);
        }
    }

    @Override
    public void update(ParticipantDTO dto) {
        log.info("TransactionCoordinatorImpl participant {}", dto);
        List<ParticipantDTO> participantDTOS = db.queryForList("SELECT * FROM `participant` WHERE bxid=:bxid",
                ImmutableMap.of("bxid", dto.getBxid()), ParticipantDTO.class);
        if (participantDTOS.size() == 1) {
            ParticipantDTO old = participantDTOS.get(0);
            if (dto.getVersion() < old.getVersion()) {
                return;
            } else {
                db.update("UPDATE `participant` SET `status`=:status , version=version+1 WHERE bxid=`bxid`", getSource(dto));
            }
        } else if (participantDTOS.size() == 0) {
            db.update("INSERT INTO  `participant` SET " +
                    "`gxid`=:gxid ," +
                    "`rxid`=:rxid ," +
                    "`bxid`=:bxid ," +
                    "`arg`=:arg ," +
                    "`serviceName`=:serviceName ," +
                    "`clzName`=:clzName ," +
                    "`beanName`=:beanName ," +
                    "`identifier`=:identifier ," +
                    "`tryMethod`=:tryMethod ," +
                    "`cancelMethod`=:cancelMethod ," +
                    "`confirmMethod`=:confirmMethod ," +
                    "`status`=:status ," +
                    "`version`=:version ", getSource(dto));
        }
    }

    private MapSqlParameterSource getSource(ParticipantDTO dto) {
        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("gxid", dto.getGxid());
        source.addValue("rxid", dto.getRxid());
        source.addValue("bxid", dto.getBxid());
        source.addValue("arg", dto.getArg());
        source.addValue("serviceName", dto.getServiceName());
        source.addValue("clzName", dto.getClzName());
        source.addValue("beanName", dto.getBeanName());
        source.addValue("identifier", dto.getIdentifier());
        source.addValue("tryMethod", dto.getTryMethod());
        source.addValue("cancelMethod", dto.getCancelMethod());
        source.addValue("confirmMethod", dto.getConfirmMethod());
        source.addValue("status", dto.getStatus());
        source.addValue("version", dto.getVersion());
        return source;
    }

    @Override
    public void clean(String gxid) {
        db.update("DELETE FROM `transaction` WHERE gxid=:gxid", ImmutableMap.of("gxid", gxid));
        db.update("DELETE FROM `participant` WHERE gxid=:gxid", ImmutableMap.of("gxid", gxid));
    }
}
