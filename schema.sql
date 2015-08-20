USE finance;
DROP TABLE IF EXISTS purchase;
CREATE TABLE purchase (
		`id` INTEGER NOT NULL AUTO_INCREMENT,
		`value` FLOAT,
		`account` INTEGER,
		`category` VARCHAR(127),
		`details` TEXT,
		`date` DATE,
    PRIMARY KEY (ID)
		);

DROP TABLE IF EXISTS credit;
CREATE TABLE credit(
		`id` INTEGER NOT NULL,
		`value` FLOAT,
		`account` INTEGER,
		`source` INTEGER,
		`date` DATE
		);

DROP TABLE IF EXISTS source;
CREATE TABLE source(
		`id` INTEGER NOT NULL,
		`name` VARCHAR(127)
		);

DROP TABLE IF EXISTS account;
CREATE TABLE account(
		`id` INTEGER NOT NULL,
		`name` VARCHAR(127),
    `balance` FLOAT
		);


DROP TABLE IF EXISTS cron_payment;
CREATE TABLE cron_payment(
		`id` INTEGER NOT NULL,
		`name` VARCHAR(127),
		`last_update` DATE
		);

