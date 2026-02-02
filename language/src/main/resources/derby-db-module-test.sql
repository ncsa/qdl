/*
    Table definition for testing the DB module round trip.
    Note that the system rejects column names that are reserved words,
    hence all of the column names are type_0

    Generally create a database, and user. Replace XXXXXXXX with the password.:

    create database qdl_test;
    create user 'qdl_tester'@'localhost' identified by 'qwert123';
    set password for 'qdl_tester'@'localhost' = 'XXXXXXXX';

    use qdl_test;
 */

CREATE TABLE qdl_test.db_test
(
    varchar_128    VARCHAR(128) PRIMARY KEY,
    smallint_0     SMALLINT,
    tinyint_0      SMALLINT,
    float_0        FLOAT,
    integer_0      INTEGER,
    numeric_0      NUMERIC(30, 20), /* 30 digits total, up to 20 (the max) after decimal point */
    double_0       DOUBLE,
    boolean_0      BOOLEAN,
    bit_0          BOOLEAN, /* Only type allowed in Derby */
    blob_0         BLOB,
    text_0         CLOB,
    timestamp_0    TIMESTAMP DEFAULT NULL,
    timestamp_long BIGINT,
    time_0         TIME,
    date_0         DATE,
    set_0          CLOB
);


/* Create a single default entry in the database. This has everything except a blob.
It's not well documented, but you can set the blob using a string that is then
turned into bytes.
 */

insert into qdl_test.db_test (varchar_128) values ('id123');

update qdl_test.db_test set blob_0 = X'DEADBEEF' where varchar_128 = 'id123';

insert into qdl_test.db_test
   (varchar_128,smallint_0,tinyint_0,float_0,
    integer_0,numeric_0,double_0,boolean_0,
    bit_0,text_0,timestamp_0,timestamp_long,
    time_0,date_0,set_0) values
    ('id123',10, 4, 4.2,
      12345,123.987654321,345.123,true,
      true, 'mairzy doats',current_timestamp,1768481743359 ,
      current_time, current_date,'{1,2,3}');
