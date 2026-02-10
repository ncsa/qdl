/*
    Table definition for testing the DB module round trip in PostgreSQL.
    Note that the system rejects column names that are reserved words,
    hence all of the column names are type_0

    Generally create a database, and user. Replace XXXXXXXX with the password.:

DROP SCHEMA IF EXISTS qdl_test CASCADE;
DROP DATABASE IF EXISTS qdl_test;
DROP USER IF EXISTS qdl_tester;
CREATE USER qdl_tester with CREATEDB PASSWORD 'XXXXXXXX';
CREATE DATABASE qdl_test WITH owner=qdl_tester;
\c qdl_test
CREATE SCHEMA qdl_test;
set search_path to qdl_test;

============== pg-connector.qdl
{
 'database':'qdl_test',
 'host':'localhost',
 'password':'XXXXXXXX',
 'port':5432,
 'schema':'qdl_test',
 'type':'postgres',
 'useSSL':false,
 'username':'qdl_tester'
}

 */

CREATE TABLE qdl_test.db_test
(
    varchar_128    VARCHAR(128) PRIMARY KEY,
    smallint_0     SMALLINT,
    tinyint_0      SMALLINT,
    float_0        REAL,
    integer_0      INTEGER,
    numeric_0      NUMERIC(64, 30), /* 64 digits total, up to 30 after decimal point */
    double_0       DOUBLE PRECISION,
    boolean_0      BOOLEAN,
    bit_0          BOOLEAN,
    blob_0         BYTEA,
    text_0         TEXT,
    timestamp_0    TIMESTAMP DEFAULT NULL,
    timestamp_long BIGINT,
    time_0         TIME,
    date_0         DATE,
    set_0          TEXT
);

GRANT ALL PRIVILEGES ON SCHEMA qdl_test TO qdl_tester;
GRANT ALL PRIVILEGES ON qdl_test.db_test TO qdl_tester;


/*
   Create a single default entry in the database. This has everything except a blob.
   It's not well documented, but you can set the blob using a string that is then
   turned into bytes.
 */

insert into qdl_test.db_test
   (varchar_128,smallint_0,tinyint_0,float_0,
    integer_0,numeric_0,double_0,boolean_0,
    bit_0,text_0,timestamp_0,timestamp_long,
    time_0,date_0,set_0, blob_0) values
    ('id123',10, 4, 4.2,
      12345,123.987654321,345.123,true,
      true, 'mairzy doats',current_timestamp,1768481743359 ,
      current_time, current_date,'{1,2,3}',
     'ΑαΒβΓγΔδΕεΖζΗηΘθϑΙιΚκϰΛλΜμΝνΞξΟοΠπϖΡρϱΣσςΤτΥυΦφΧχΨψΩω');
