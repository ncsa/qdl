/*
   Setup for testing the Derby database module.
   
      ******* My test stuff

   BOOT_PASSWORD = XXXXXXX
   USER_PASSWORD = YYYYYYYY
 

NOTE: This installs the database to /home/ncsa/dev/derby/qdl_test so if you want it elsewhere
change that.

 **  start ij and enter

connect 'jdbc:derby:/home/ncsa/dev/derby/qdl_test;create=true;dataEncryption=true;bootPassword=XXXXXXX;user=qdl_tester';
CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.connection.requireAuthentication', 'true');
CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.database.sqlAuthorization','true');
CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.authentication.provider', 'BUILTIN');
CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.user.qdl_tester', 'YYYYYYYY');
CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.database.propertiesOnly', 'true');

 **  test it worked with
create schema qdl_test;
show schemas;

 **  exit ij with

exit;

  ** Now the full connection string is

connect 'jdbc:derby:/home/ncsa/dev/derby/qdl_test;user=qdl_tester;password=YYYYYYYY;bootPassword=XXXXXXX';

 ** and you can create the database by issuing this in ij

ij run '/home/ncsa/dev/ncsa-git/qdl/language/src/main/resources/derby-db-module-test.sql';

   ============ derby-connector.qdl

{
 'database':'/home/ncsa/dev/derby/qdl_test',
 'host':'localhost',
 'password':'YYYYYYYY',
 'bootPassword':'XXXXXXXX',
 'port':3306,
 'schema':'qdl_test',
 'storeType':'file',
 'type':'derby',
 'useSSL':false,
 'username':'qdl_tester'
}

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

CREATE TABLE qdl_test.xxx (id varchar(20) NOT NULL PRIMARY KEY,name varchar(128) DEFAULT NULL);
qdl_test=# GRANT ALL PRIVILEGES ON qdl_test.xxx TO qdl_tester;