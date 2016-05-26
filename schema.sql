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
  active BOOL DEFAULT TRUE,
  PRIMARY KEY(id)
);

DROP TABLE IF EXISTS category_budget;
CREATE TABLE category_budget(
  id INTEGER AUTO_INCREMENT NOT NULL,
  start TIMESTAMP,
  balance FLOAT,
  budget FLOAT,
  category INTEGER,
  pid INTEGER,
  PRIMARY KEY(id),
  FOREIGN KEY(category) REFERENCES category(id),
  FOREIGN KEY(pid) REFERENCES category_budget(id)
);

DROP TABLE IF EXISTS income;
CREATE TABLE income(
  id INTEGER AUTO_INCREMENT NOT NULL,
  value FLOAT,
  account INTEGER,
  source INTEGER,
  date TIMESTAMP,
  period DATE,
  PRIMARY KEY(id),
  FOREIGN KEY(account) REFERENCES account(id)
);

DROP TABLE IF EXISTS purchase;
CREATE TABLE purchase (
  id INTEGER AUTO_INCREMENT NOT NULL,
  value FLOAT,
  account INTEGER,
  category_budget INTEGER,
  details TEXT,
  date TIMESTAMP,
  PRIMARY KEY (id),
  FOREIGN KEY(account) REFERENCES account(id),
  FOREIGN KEY(category_budget) REFERENCES category_budget(id)
);

DELIMITER $

DROP PROCEDURE IF EXISTS update_category_budget $
CREATE PROCEDURE update_category_budget()
  BEGIN
    DECLARE iteration TIMESTAMP DEFAULT NOW();
    INSERT INTO category_budget(start,balance,budget,category)
      (SELECT iteration, 0.0, budget, category FROM category_budget
        JOIN category ON (category.active IS TRUE AND category.id = category_budget.category)
        WHERE start=(
          SELECT MAX(start) FROM category_budget
        )
      );
  END $

DROP PROCEDURE IF EXISTS check_time $
CREATE PROCEDURE check_time()
  BEGIN
    DECLARE q TIMESTAMP;
    SET q = (
      SELECT MAX(start) FROM category_budget
    );
    IF q IS NULL OR q < (DATE_SUB(CURRENT_DATE(), INTERVAL 1 MONTH)) THEN
      CALL update_category_budget();
    END IF;
  END $

DROP EVENT IF EXISTS update_category_budget_event $
CREATE EVENT update_category_budget_event
  ON SCHEDULE EVERY '1' SECOND STARTS NOW() DO
  BEGIN
    call check_time();
  END $

DELIMITER ;

INSERT INTO account(name,balance,description) VALUES('checking', 5000.0, 'Jonathan Hamm\'s Checking Account');
INSERT INTO account(name,balance,description) VALUES('savings', 650.0, 'Jonathan Hamm\'s Savings Account');

INSERT INTO category(name,active) VALUES('BOB', TRUE);
INSERT INTO category(name,active) VALUES('asdf', TRUE);
INSERT INTO category(name,active) VALUES('q3r', TRUE);
INSERT INTO category(name,active) VALUES('2314', TRUE);
INSERT INTO category(name,active) VALUES('qwer', TRUE);
INSERT INTO category(name,active) VALUES('qwerdi', TRUE);
INSERT INTO category(name,active) VALUES('derrp', TRUE);


INSERT INTO category_budget(start,balance,budget,category,pid) VALUES(NOW(), 0.0, 00.0, (SELECT id FROM category WHERE name='BOB'), NULL);
INSERT INTO category_budget(start,balance,budget,category,pid) VALUES(NOW(), 0.0, 500.0, (SELECT id FROM category WHERE name='BOB'), NULL);
INSERT INTO category_budget(start,balance,budget,category,pid) VALUES(NOW(), 0.0, 300.0, (SELECT id FROM category WHERE name='asdf'), 1);
INSERT INTO category_budget(start,balance,budget,category,pid) VALUES(NOW(),  0.0, 300.0, (SELECT id FROM category WHERE name='q3r'), 3);
INSERT INTO category_budget(start,balance,budget,category,pid) VALUES(NOW(), 0.0, 300.0, (SELECT id FROM category WHERE name='2314'), 2);
INSERT INTO category_budget(start,balance,budget,category,pid) VALUES(NOW(),  0.0, 300.0, (SELECT id FROM category WHERE name='qwer'), 2);
INSERT INTO category_budget(start,balance,budget,category,pid) VALUES(NOW(),  0.0, 300.0, (SELECT id FROM category WHERE name='qwerdi'), 1);
INSERT INTO category_budget(start,balance,budget,category,pid) VALUES(NOW(),  0.0, 300.0, (SELECT id FROM category WHERE name='derrp'), 3);


SET GLOBAL event_scheduler = 1;

