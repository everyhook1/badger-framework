package org.badger.tcc.coordinator.service;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.badger.common.api.RpcProvider;
import org.badger.common.api.RpcRequest;
import org.badger.common.api.SpanContext;
import org.badger.common.api.util.SnowflakeIdWorker;
import org.badger.tcc.entity.ParticipantDTO;
import org.badger.tcc.entity.TransactionDTO;
import org.badger.tcc.entity.TransactionStatus;
import org.badger.tcc.spring.TransactionCoordinator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author liubin01
 */
@Slf4j
@RpcProvider
@Service
public class TransactionCoordinatorImpl implements TransactionCoordinator {

    @Autowired
    private NamedParameterJdbcTemplate db;

    @Autowired
    private CuratorFramework curatorFramework;

    private static final String LEADER_PATH = "/tcc-coordinator/leader";

    private LeaderLatch leaderLatch;

    @PostConstruct
    public void init() throws Exception {
        leaderLatch = new LeaderLatch(curatorFramework, LEADER_PATH);
        leaderLatch.start();
    }

    private static final ThreadPoolExecutor THREAD_POOL =
            new ThreadPoolExecutor(32, 32, 0, TimeUnit.MICROSECONDS, new LinkedBlockingDeque<>(512));

    private static final RowMapper<ParticipantDTO> PARTICIPANT_ROW_MAPPER = (rs, rowNum) -> {
        ParticipantDTO dto = new ParticipantDTO();
        dto.setBxid(rs.getString("bxid"));
        dto.setGxid(rs.getString("gxid"));
        dto.setRxid(rs.getString("rxid"));
        dto.setStatus(rs.getInt("status"));
        dto.setServiceName(rs.getString("serviceName"));
        dto.setPayload(rs.getBytes("payload"));
        dto.setVersion(rs.getInt("version"));
        return dto;
    };

    private static final RowMapper<TransactionDTO> TRANSACTION_ROW_MAPPER = (rs, rowNum) -> {
        TransactionDTO dto = new TransactionDTO();
        dto.setGxid(rs.getString("gxid"));
        dto.setRxid(rs.getString("rxid"));
        dto.setStatus(rs.getInt("status"));
        return dto;
    };

    @Override
    public TransactionDTO getTransaction(String gxid) {
        log.info("TransactionCoordinatorImpl getTransaction {}", gxid);
        List<TransactionDTO> transactionDTOS = db.query("SELECT * FROM `transaction` WHERE gxid=:gxid",
                ImmutableMap.of("gxid", gxid), TRANSACTION_ROW_MAPPER);
        if (transactionDTOS.size() == 0) {
            return null;
        }
        TransactionDTO transactionDTO = transactionDTOS.get(0);
        List<ParticipantDTO> participantDTOS = db.query("SELECT * FROM `participant` WHERE gxid=:gxid",
                ImmutableMap.of("gxid", gxid), PARTICIPANT_ROW_MAPPER);
        transactionDTO.setParticipantDTOS(participantDTOS);
        return transactionDTO;

    }

    @Override
    public void update(TransactionDTO transactionDTO) {
        log.info("TransactionCoordinatorImpl transaction {}", transactionDTO);
        List<TransactionDTO> transactionDTOS = db.query("SELECT * FROM `transaction` WHERE gxid=:gxid",
                ImmutableMap.of("gxid", transactionDTO.getGxid()), TRANSACTION_ROW_MAPPER);
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
        List<ParticipantDTO> participantDTOS = db.query("SELECT * FROM `participant` WHERE bxid=:bxid",
                ImmutableMap.of("bxid", dto.getBxid()), PARTICIPANT_ROW_MAPPER);
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
                    "`payload`=:payload ," +
                    "`serviceName`=:serviceName ," +
                    "`status`=:status ," +
                    "`version`=:version ", getSource(dto));
            SpanContext.getClient().addListener(dto.getServiceName());
        }
    }

    private MapSqlParameterSource getSource(ParticipantDTO dto) {
        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("gxid", dto.getGxid());
        source.addValue("rxid", dto.getRxid());
        source.addValue("bxid", dto.getBxid());
        source.addValue("status", dto.getStatus());
        source.addValue("version", dto.getVersion());
        source.addValue("serviceName", dto.getServiceName());
        source.addValue("payload", dto.getPayload());
        return source;
    }

    public void clean(String gxid) {
        TransactionDTO transactionDTO = getTransaction(gxid);
        if (transactionDTO.getParticipantDTOS().stream().allMatch(o -> o.getStatus() == transactionDTO.getStatus())) {
            db.update("DELETE FROM `transaction` WHERE gxid=:gxid", ImmutableMap.of("gxid", gxid));
            db.update("DELETE FROM `participant` WHERE gxid=:gxid", ImmutableMap.of("gxid", gxid));
        }
    }

    private void transactionStatusChange(String gxid, int status) {
        db.update("update `transaction` SET `status` = :status WHERE `gxid`=:gxid",
                ImmutableMap.of("status", status, "gxid", gxid));
    }

    @Override
    public void commit(String gxid) {
        transactionStatusChange(gxid, TransactionStatus.TRY_SUCCESS.toInt());
        TransactionDTO transactionDTO = getTransaction(gxid);
        List<ParticipantDTO> participantDTOS = transactionDTO.getParticipantDTOS();
        THREAD_POOL.submit(() -> {
            try {
                for (ParticipantDTO participantDTO : participantDTOS) {
                    RpcRequest request = new RpcRequest();
                    transactionDTO.setParticipantDTOS(Collections.singletonList(participantDTO));
                    request.setClzName("ResourceManager");
                    request.setMethod("commit");
                    request.setServiceName(participantDTO.getServiceName());
                    request.setArgs(new Object[]{transactionDTO});
                    request.setArgTypes(new Class<?>[]{TransactionDTO.class});
                    request.setSeqId(SnowflakeIdWorker.getId());
                    request.setParentRpc(SpanContext.getCurRequest());
                    SpanContext.getClient().send(request);
                }
                transactionStatusChange(gxid, TransactionStatus.CONFIRM_SUCCESS.toInt());
            } catch (Exception e) {
                log.error("rollback failed gxid {}", gxid, e);
                transactionStatusChange(gxid, TransactionStatus.CANCEL_FAILED.toInt());
            }
        });
    }

    @Override
    public void rollback(String gxid) {
        transactionStatusChange(gxid, TransactionStatus.TRY_FAILED.toInt());
        TransactionDTO transactionDTO = getTransaction(gxid);
        List<ParticipantDTO> participantDTOS = transactionDTO.getParticipantDTOS();
        THREAD_POOL.submit(() -> {
            try {
                for (ParticipantDTO participantDTO : participantDTOS) {
                    RpcRequest request = new RpcRequest();
                    transactionDTO.setParticipantDTOS(Collections.singletonList(participantDTO));
                    request.setClzName("ResourceManager");
                    request.setMethod("rollback");
                    request.setServiceName(participantDTO.getServiceName());
                    request.setArgs(new Object[]{transactionDTO});
                    request.setArgTypes(new Class<?>[]{TransactionDTO.class});
                    request.setSeqId(SnowflakeIdWorker.getId());
                    request.setParentRpc(SpanContext.getCurRequest());
                    SpanContext.getClient().send(request);
                }
                transactionStatusChange(gxid, TransactionStatus.CANCEL_SUCCESS.toInt());
            } catch (Exception e) {
                log.error("rollback failed gxid {}", gxid, e);
                transactionStatusChange(gxid, TransactionStatus.CANCEL_FAILED.toInt());
            }
        });
    }

    @Scheduled(cron = "0 * * * * ?")
    public void execute() {
        if (!leaderLatch.hasLeadership()) {
            log.info("not the leader");
            return;
        }
        log.info("the leader");

        List<TransactionDTO> transactionDTOS = db.query("SELECT * FROM `transaction` LIMIT 100", TRANSACTION_ROW_MAPPER);
        if (transactionDTOS.size() == 0) {
            return;
        }
        for (TransactionDTO transactionDTO : transactionDTOS) {
            if (transactionDTO.getStatus() == TransactionStatus.CONFIRM_SUCCESS.toInt() ||
                    transactionDTO.getStatus() == TransactionStatus.CANCEL_SUCCESS.toInt()) {
                clean(transactionDTO.getGxid());
            }
        }
    }
}
