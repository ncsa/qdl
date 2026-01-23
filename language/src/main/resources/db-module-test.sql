/*
    Table definition for testing the DB module round trip.
    Note that the system rejects column names that are reserved words,
    hence all of the column names are type_0
 */

CREATE TABLE oauth2.db_test
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

GRANT
All
ON oauth2.db_test TO 'oa4mp-server'@'localhost';

/* Create a single entry in the database. This has everything except a blob.
It's not well documented, but you can set the blob using a string that is then
turned into bytes.

insert into oauth2.db_test
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