create database if not exists coviddb;
use coviddb;
DROP TABLE IF EXISTS covid;
CREATE EXTERNAL TABLE IF NOT EXISTS covid (
  event_date DATE,
  location STRING,
  total_cases INT,
  new_cases INT,
  total_deaths INT,
  new_deaths INT,
  new_cases_per_million INT
)
row format delimited
fields terminated by ','
lines terminated by '\n'
stored as textfile location 'hdfs://namenode:9000/user/hive/warehouse/covid.db/covid';
