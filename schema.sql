SET GLOBAL event_scheduler = 1;
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
  PRIMARY KEY(id)
);

DROP TABLE IF EXISTS category_budget;
CREATE TABLE category_budget(
  id INTEGER AUTO_INCREMENT NOT NULL,
  start DATE,
  balance FLOAT,
  budget FLOAT,
  category INTEGER,
  PRIMARY KEY(id),
  FOREIGN KEY(category) REFERENCES category(id)
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
  FOREIGN KEY(account) REFERENCES account(id),
  FOREIGN KEY(category) REFERENCES category(id)
);

DELIMITER //
CREATE PROCEDURE update_category_budget()
  BEGIN
    INSERT INTO category_budget(start,balance,budget,category)
      (SELECT NOW()) UNION (SELECT 0.0) UNION
        (SELECT budget, category FROM category_budget WHERE start=(
          SELECT MAX(start) FROM category_budget
        )
      );
  END//

DELIMITER //
CREATE PROCEDURE check_time()
  BEGIN
    DECLARE q DATE;
    SET q = (
      SELECT MAX(start) FROM category_budget
    );
    IF q IS NULL OR q < (DATE_SUB(CURRENT_DATE(), INTERVAL 1 MONTH)) THEN
      CALL update_category_budget();
    END IF;
  END//

CREATE EVENT update_category_budget_event
  ON SCHEDULE EVERY '1' MINUTE STARTS NOW() DO
  BEGIN
    call update_category_budget();
  END;

INSERT INTO account(name,balance,description) VALUES('checking', 5000.0, 'Jonathan Hamm\'s Checking Account');
INSERT INTO account(name,balance,description) VALUES('savings', 650.0, 'Jonathan Hamm\'s Savings Account');

#INSERT INTO category(name,balance,budget) VALUES('food', 0, 5000)