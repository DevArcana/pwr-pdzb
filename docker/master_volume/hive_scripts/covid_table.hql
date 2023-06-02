create database if not exists coviddb;
use coviddb;
DROP TABLE IF EXISTS covid;
CREATE TABLE IF NOT EXISTS covid (
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
stored as textfile;

DROP TABLE IF EXISTS calculated;
CREATE TABLE IF NOT EXISTS calculated (
  event_date DATE,
  location STRING,
  total_cases INT,
  new_cases INT,
  total_deaths INT,
  new_deaths INT,
  new_cases_per_million INT,
  average_new_cases_per_million FLOAT
)
row format delimited
fields terminated by ','
lines terminated by '\n'
stored as textfile;