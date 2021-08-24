package org.badger.provider;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.badger.common.api.RpcProvider;
import org.badger.common.api.transaction.Compensable;
import org.badger.example.api.TccProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * @author liubin01
 */
@RpcProvider(qualifier = "tccProvider")
@Slf4j
@Service
public class TccProviderImpl implements TccProvider {

    @Autowired
    private NamedParameterJdbcTemplate db;

    static class DataB {
        int id;
        int cnt;
        int reserving;
        int version;
    }


    @Override
    @Compensable(identifier = "TccProvider", tryMethod = "tryProvider", confirmMethod = "confirmProvider", cancelMethod = "cancelProvider")
    public void tryProvider(int a, int b) {
        log.info("tryProvider {} {}", a, b);
        DataB dataB = db.queryForObject("select * from dbb where id = :id", ImmutableMap.of("id", a), (rs, rowNum) -> {
            DataB obj = new DataB();
            obj.id = rs.getInt("id");
            obj.cnt = rs.getInt("cnt");
            obj.reserving = rs.getInt("reserving");
            obj.version = rs.getInt("version");
            return obj;
        });
        assert dataB != null;
        if ((dataB.cnt - dataB.reserving) < b) {
            throw new RuntimeException("DataB.cnt < b" + dataB + " " + b);
        }
        int res = db.update("update dbb set reserving = reserving + :b,`version` = `version` + 1  where id = :id and `version` = :version", ImmutableMap.of("id", a, "b", b, "version", dataB.version));
        if (res == 0) {
            throw new RuntimeException("not change success" + dataB + " " + b);
        }
    }

    @Override
    @Compensable(identifier = "TccProvider", tryMethod = "tryProvider", confirmMethod = "confirmProvider", cancelMethod = "cancelProvider")
    public void confirmProvider(int a, int b) {
        log.info("tryProvider {} {}", a, b);
        db.update("update dbb set cnt = cnt - :b, reserving = reserving - :b,`version` = `version` + 1 where id = :id", ImmutableMap.of("id", a, "b", b));
    }

    @Override
    @Compensable(identifier = "TccProvider", tryMethod = "tryProvider", confirmMethod = "confirmProvider", cancelMethod = "cancelProvider")
    public void cancelProvider(int a, int b) {
        log.info("tryProvider {} {}", a, b);
        db.update("update dbb set reserving = reserving - :b,`version` = `version` + 1 where id = :id", ImmutableMap.of("id", a, "b", b));
    }
}
