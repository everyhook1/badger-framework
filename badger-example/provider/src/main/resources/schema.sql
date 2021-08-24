DROP TABLE if exists `dbb`;
create table if not exists `dbb`
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