package org.badger.consumer.tcc;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.badger.common.api.RpcProxy;
import org.badger.common.api.transaction.Compensable;
import org.badger.example.api.TccBackend;
import org.badger.example.api.TccProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * @author liubin01
 */
@Slf4j
@Service
public class AccountService {

    @Autowired
    @RpcProxy(serviceName = "badger-backend", qualifier = "tccBackend")
    private TccBackend tccBackend;

    @Autowired
    @RpcProxy(serviceName = "badger-example", qualifier = "tccProvider")
    private TccProvider tccProvider;

    @Autowired
    private NamedParameterJdbcTemplate db;

    static class DataA {
        int id;
        int cnt;
        int reserving;
        int version;
    }

    @Compensable(identifier = "account", tryMethod = "tryM", confirmMethod = "confirmM", cancelMethod = "cancelM")
    public void tryM(int a, int b) {
        log.info("tryM {} {}", a, b);
        DataA dataA = db.queryForObject("select * from dba where id = :id", ImmutableMap.of("id", a),
                (rs, rowNum) -> {
                    DataA obj = new DataA();
                    obj.id = rs.getInt("id");
                    obj.cnt = rs.getInt("cnt");
                    obj.reserving = rs.getInt("reserving");
                    obj.version = rs.getInt("version");
                    return obj;
                });
        assert dataA != null;
        if ((dataA.cnt - dataA.reserving) < b) {
            throw new RuntimeException("DataA.cnt < b" + dataA + " " + b);
        }
        tccProvider.tryProvider(a, b);
        tccBackend.tryBackend(a, b);
        int res = db.update("update dba set reserving = reserving + :b,`version` = `version` + 1  where id = :id and `version` = :version", ImmutableMap.of("id", a, "b", b, "version", dataA.version));
        if (res == 0) {
            throw new RuntimeException("not change success" + dataA + " " + b);
        }
    }

    @Compensable(identifier = "account", tryMethod = "tryM", confirmMethod = "confirmM", cancelMethod = "cancelM")
    public void confirmM(int a, int b) {
        log.info("confirmM {} {}", a, b);
        db.update("update dba set cnt = cnt - :b, reserving = reserving - :b,`version` = `version` + 1 where id = :id", ImmutableMap.of("id", a, "b", b));
    }

    @Compensable(identifier = "account", tryMethod = "tryM", confirmMethod = "confirmM", cancelMethod = "cancelM")
    public void cancelM(int a, int b) {
        log.info("cancelM {} {}", a, b);
        db.update("update dba set reserving = reserving - :b,`version` = `version` + 1 where id = :id", ImmutableMap.of("id", a, "b", b));
    }

}
