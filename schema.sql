DROP DATABASE IF EXISTS finance_dev;
CREATE DATABASE finance_dev;
USE finance_dev;

DROP TABLE IF EXISTS account;
CREATE TABLE account(
		id INTEGER AUTO_INCREMENT NOT NULL,
		name VARCHAR(127),
    balance FLOAT,
    description VARCHAR(256),
    PRIMARY KEY(id)
);

DROP TABLE IF EXISTS category;
CREATE TABLE category(
    id INTEGER AUTO_INCREMENT NOT NULL,
    name VARCHAR(255),
    budget FLOAT,
    PRIMARY KEY(id)
);

DROP TABLE IF EXISTS income;
CREATE TABLE income(
  id INTEGER AUTO_INCREMENT NOT NULL,
  value FLOAT,
  account INTEGER,
  source INTEGER,
  date DATE,
  period DATE,
  PRIMARY KEY(id),
  FOREIGN KEY(account) REFERENCES account(id)
);

DROP TABLE IF EXISTS purchase;
CREATE TABLE purchase (
  id INTEGER AUTO_INCREMENT NOT NULL,
  value FLOAT,
  account INTEGER,
  category INTEGER,
  details TEXT,
  date DATE,
  PRIMARY KEY (id),
  FOREIGN KEY(category) REFERENCES category(id)
);
