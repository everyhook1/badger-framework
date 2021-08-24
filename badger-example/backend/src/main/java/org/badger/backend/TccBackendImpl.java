package org.badger.backend;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.badger.common.api.RpcProvider;
import org.badger.common.api.transaction.Compensable;
import org.badger.example.api.TccBackend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * @author liubin01
 */
@RpcProvider(qualifier = "tccBackend")
@Slf4j
@Service
public class TccBackendImpl implements TccBackend {

    @Autowired
    private NamedParameterJdbcTemplate db;

    static class DataC {
        int id;
        int cnt;
        int reserving;
        int version;
    }


    @Override
    @Compensable(identifier = "TccBackend", tryMethod = "tryBackend", confirmMethod = "confirmBackend", cancelMethod = "cancelBackend")
    public void tryBackend(int a, int b) {
        log.info("tryBackend {} {}", a, b);
        DataC dataC = db.queryForObject("select * from dbc where id = :id", ImmutableMap.of("id", a), (rs, rowNum) -> {
            DataC obj = new DataC();
            obj.id = rs.getInt("id");
            obj.cnt = rs.getInt("cnt");
            obj.reserving = rs.getInt("reserving");
            obj.version = rs.getInt("version");
            return obj;
        });
        assert dataC != null;
        if ((dataC.cnt - dataC.reserving) < b) {
            throw new RuntimeException("DataC.cnt < b" + dataC + " " + b);
        }
        int res = db.update("update dbc set reserving = reserving + :b,`version` = `version` + 1  where id = :id and `version` = :version", ImmutableMap.of("id", a, "b", b, "version", dataC.version));
        if (res == 0) {
            throw new RuntimeException("not change success" + dataC + " " + b);
        }
    }

    @Override
    @Compensable(identifier = "TccBackend", tryMethod = "tryBackend", confirmMethod = "confirmBackend", cancelMethod = "cancelBackend")
    public void confirmBackend(int a, int b) {
        log.info("confirmBackend {} {}", a, b);
        db.update("update dbc set cnt = cnt - :b, reserving = reserving - :b,`version` = `version` + 1 where id = :id", ImmutableMap.of("id", a, "b", b));
    }

    @Override
    @Compensable(identifier = "TccBackend", tryMethod = "tryBackend", confirmMethod = "confirmBackend", cancelMethod = "cancelBackend")
    public void cancelBackend(int a, int b) {
        log.info("cancelBackend {} {}", a, b);
        db.update("update dbc set reserving = reserving - :b,`version` = `version` + 1 where id = :id", ImmutableMap.of("id", a, "b", b));
    }
}
