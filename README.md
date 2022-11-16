# badger-framework

A simple Netty Rpc implement tcc <br/>
All the examples are implemented in the module badger-example

## Performance

![avatar](./docs/system.png)
![avatar](./docs/jmeter.png)

## How To Use RPC

### provider

```yaml
rpc:
  serviceName: badger-example
  port: 11311

zk:
  address: 127.0.0.1:2181
```

```java

@RpcProvider
public interface UserInfo {
    String echo(String str);
}

```

### consumer

```yaml
zk:
  address: 127.0.0.1:2181
```

```java

@RpcProxy(serviceName = "badger-example")
public interface UserInfo {
    String echo(String str);
}

```

assume you already have a local zookeeper.

```shell
start zookeeper
start provider 
start consumer 
curl http://127.0.0.1:8080/echo?str=abc
```

then you would get response:
echo from server abc

```jmx
jmeter -n -t rpcTest.jmx -l a -e -o b
```

## How To Run Tcc Example

1) Run badger-tcc-coordinator
2) Run badger-example/consumer

```sql
create table if not exists `dba`
(
    `id`        BIGINT unsigned NOT NULL AUTO_INCREMENT,
    `cnt`       INT             NOT NULL,
    `reserving` INT             NOT NULL,
    `version`   INT             NOT NULL,
    `dbctime`   DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    `dbutime`   DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4;
INSERT INTO `dba`
SET `id`        = 1,
    `cnt`       = 100,
    `reserving` = 0,
    `version`   = 0;
```

```java
    @Autowired
    @RpcProxy(serviceName = "badger-backend", qualifier = "tccBackend")
    private TccBackend tccBackend;
    
    @Autowired
    @RpcProxy(serviceName = "badger-example", qualifier = "tccProvider")
    private TccProvider tccProvider;
    
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
```

3) Run badger-example/provider
4) Run badger-example/backend
<br/>then execute the shell below two times
```shell
 curl http://127.0.0.1:8080/try?a=1&b=50
```
first time will execute success,second execute failed.
## Kubernetes

assume you already install docker and kind.

```shell
# create a cluster with local registry
sh kind-with-registry.sh
sh deploy.sh
kubectl port-forward service/consumer 8080:8080
curl http://127.0.0.1:8080/echo?str=abc
```

## RoadMap

- [ ] trace
- [X] transaction

## HAVE FUN!
