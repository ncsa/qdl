/*
    Table definition for testing the DB module round trip.
    Note that the system rejects column names that are reserved words,
    hence all of the column names are type_0

    Generally create a database, and user. Replace XXXXXXXX with the password.:

    create database qdl_test;
    create user 'qdl_tester'@'localhost' identified by 'qwert123';
    set password for 'qdl_tester'@'localhost' = 'XXXXXXXX';

    use qdl_test;


    ============== mysql-connector.qdl
{
 'database':'qdl_test',
 'host':'localhost',
 'password':'XXXXXXXX',
 'port':3306,
 'schema':'qdl_test',
 'type':'mysql',
 'useSSL':false,
 'username':'qdl_tester'
}

 */

CREATE TABLE qdl_test.db_test
(
    varchar_128    VARCHAR(128) PRIMARY KEY,
    smallint_0     SMALLINT,
    tinyint_0      TINYINT,
    float_0        FLOAT,
    integer_0      INTEGER,
    numeric_0      NUMERIC(64, 30), /* 64 digits total, up to 30 (the max) after decimal point */
    double_0       DOUBLE,
    boolean_0      BOOLEAN,
    bit_0          BIT,
    blob_0         BLOB,
    text_0         TEXT,
    timestamp_0    TIMESTAMP DEFAULT NULL,
    timestamp_long BIGINT,
    time_0         TIME,
    date_0         DATE,
    set_0          TEXT
);

/*  This is created (then dropped) on the fly by the DB module tests, so it is here for reference.
CREATE TABLE qdl_test.xxx (
                       id varchar(20) NOT NULL PRIMARY KEY,
                       name varchar(128) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
*/

GRANT All PRIVILEGES ON qdl_test.* TO 'qdl_tester'@'localhost';
grant create on qdl_test.* to 'qdl_tester'@'localhost';

/* Create a single default entry in the database. This has everything except a blob.
It's not well documented, but you can set the blob using a string that is then
turned into bytes.

insert into qdl_test.db_test
   (varchar_128,smallint_0,tinyint_0,float_0,
    integer_0,numeric_0,double_0,boolean_0,
    bit_0,text_0,timestamp_0,timestamp_long,
    time_0,date_0,set_0, blob_0) values
    ('id123',1000, 4, 4.2,
      12345,123.987654321,345.123,1,
      1, 'mairzy doats',now(),1768481743359 ,
      now(), now(),'{1,2,3}',
      'ΑαΒβΓγΔδΕεΖζΗηΘθϑΙιΚκϰΛλΜμΝνΞξΟοΠπϖΡρϱΣσςΤτΥυΦφΧχΨψΩω');
 */

/*
    Test table for document support. Run as root, create table, grant qdl_tester access!
*/

CREATE TABLE qdl_test.doc_test
(
    id            VARCHAR(128) PRIMARY KEY,
    last_name     VARCHAR(250),
    first_name    VARCHAR(250),
    state         TEXT,
    create_date   BIGINT
);

GRANT All PRIVILEGES ON qdl_test.doc_test TO 'qdl_tester'@'localhost';
