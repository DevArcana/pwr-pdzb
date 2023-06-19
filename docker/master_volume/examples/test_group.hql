use testdb;
INSERT OVERWRITE DIRECTORY 'hdfs://namenode:9000/user/hive/warehouse/results' SELECT max(salary), storeid FROM employee GROUP BY storeid;


