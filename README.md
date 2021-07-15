# badger-framework

A simple Netty Rpc

## How To Use

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
curl http://127.0.0.1:8081/?name=abc
```

then you would get response:
echo from server abc

## HAVE FUN!