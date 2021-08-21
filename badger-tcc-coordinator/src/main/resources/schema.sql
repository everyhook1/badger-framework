DROP TABLE IF EXISTS `transaction`;
create table if not exists `transaction`
(
    `id`      BIGINT unsigned NOT NULL AUTO_INCREMENT,
    `gxid`    VARCHAR(128)    NOT NULL,
    `rxid`    VARCHAR(128)    NOT NULL,
    `status`  TINYINT         NOT NULL,
    `dbctime` DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    `dbutime` DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY `idx_gxid` (`gxid`),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4;
DROP TABLE IF EXISTS `participant`;
create table if not exists `participant`
(
    `id`          BIGINT unsigned NOT NULL AUTO_INCREMENT,
    `gxid`        VARCHAR(128)    NOT NULL,
    `rxid`        VARCHAR(128)    NOT NULL,
    `bxid`        VARCHAR(128)    NOT NULL,
    `payload`     BLOB            NOT NULL,
    `serviceName` VARCHAR(128)    NOT NULL,
    `status`      TINYINT         NOT NULL,
    `version`     INT             NOT NULL,
    `dbctime`     DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    `dbutime`     DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    KEY `idx_gxid` (`gxid`),
    UNIQUE KEY `idx_bxid` (`bxid`),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4;