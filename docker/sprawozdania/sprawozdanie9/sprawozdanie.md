# Sprawozdanie 9

**Grupa A3:**

inż. Michał Liss

inż. Marceli Sokólski

inż. Piotr Krzystanek

## Skrypty Hive

### Covid

```sql
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

DROP TABLE IF EXISTS covid_raw;
CREATE TABLE IF NOT EXISTS covid_raw (
     json STRING 
);

LOAD data LOCAL inpath "/data/master_volume/datasets/covid-dataset.jsonl" into table covid_raw;

-- LOAD DATA
INSERT INTO covid
SELECT t1.`a`, t1.`b`, t1.`c`, t1.`d`, t1.`e`, t1.`f`, t1.`g`
    FROM covid_raw t 
        LATERAL VIEW json_tuple(t.json, 'date', 'location', 'total_cases', 'new_cases', 'total_deaths', 'new_deaths', 'new_cases_per_million') t1 as `a`, `b`, `c`, `d`, `e`, `f`, `g`

INSERT OVERWRITE TABLE calculated 
SELECT * FROM (
  WITH transformed_data AS (
    SELECT
      event_date,
      location,
      CAST(total_cases AS INT) AS total_cases,
      CAST(new_cases AS INT) AS new_cases,
      CAST(total_deaths AS INT) AS total_deaths,
      CAST(new_deaths AS INT) AS new_deaths,
      CASE WHEN new_cases_per_million = '' THEN 0 ELSE CAST(new_cases_per_million AS INT) END AS new_cases_per_million
    FROM covid
  )
  SELECT
    event_date,
    location,
    total_cases,
    new_cases,
    total_deaths,
    new_deaths,
    new_cases_per_million,
    avg(new_cases_per_million) OVER () AS average_new_cases_per_million
  FROM transformed_data
) t;
```

### Steam
```sql
create database if not exists steamdb;
use steamdb;

-- RAW TABLE
DROP TABLE IF EXISTS steam_store_raw;
CREATE TABLE IF NOT EXISTS steam_store_raw (
     json STRING 
);

LOAD data LOCAL inpath "/data/master_volume/datasets/steam-dataset/steam_dataset/appinfo/store_data/steam_store_data.jsonl" into table steam_store_raw;

-- PARSED TABLE
DROP TABLE IF EXISTS steam_store;
CREATE TABLE IF NOT EXISTS steam_store (
     steam_appid INT,
     coming_soon BOOLEAN,
     `date` STRING
);

-- LOAD DATA
INSERT INTO steam_store
SELECT t2.`steam_appid`, t3.`coming_soon`, t3.`date`
    FROM steam_store_raw t 
        LATERAL VIEW json_tuple(t.json, 'key', 'value') t1 as `key`, `value` 
        LATERAL VIEW json_tuple(t1.`value`, 'steam_appid', 'release_date') t2 as `steam_appid`, `release_date`
        LATERAL VIEW json_tuple(t2.`release_date`, 'coming_soon', 'date') t3 as `coming_soon`, `date`;

-- RAW TABLE
DROP TABLE IF EXISTS steam_spy_raw;
CREATE TABLE IF NOT EXISTS steam_spy_raw (
     json STRING 
);

LOAD data LOCAL inpath "/data/master_volume/datasets/steam-dataset/steam_dataset/steamspy/basic/steam_spy_scrap.jsonl" into table steam_spy_raw;

-- PARSED TABLE
DROP TABLE IF EXISTS steam_spy;
CREATE TABLE IF NOT EXISTS steam_spy (
    appid INT,
    name STRING,
    developer STRING,
    publisher STRING,
    score_rank STRING,
    positive INT,
    negative INT,
    userscore INT,
    owners STRING,
    average_forever INT,
    average_2weeks INT,
    median_forever INT,
    median_2weeks INT,
    price STRING,
    initialprice STRING,
    discount STRING,
    ccu INT
);

-- LOAD DATA
INSERT INTO steam_spy
SELECT t2.`appid`, t2.`name`, t2.`developer`, t2.`publisher`, t2.`score_rank`, t2.`positive`, t2.`negative`, t2.`userscore`, t2.`owners`, t2.`average_forever`, t2.`average_2weeks`, t2.`median_forever`, t2.`median_2weeks`, t2.`price`, t2.`initialprice`, t2.`discount`, t2.`ccu`
    FROM steam_spy_raw t 
        LATERAL VIEW json_tuple(t.json, 'key', 'value') t1 as `key`, `value` 
        LATERAL VIEW json_tuple(
            t1.`value`, 
            'appid',
            'name',
            'developer',
            'publisher', 
            'score_rank', 
            'positive', 
            'negative',
            'userscore',
            'owners', 
            'average_forever',
            'average_2weeks',
            'median_forever',
            'median_2weeks',
            'price',
            'initialprice',
            'discount',
            'ccu') t2 as `appid`, `name`, `developer`, `publisher`, `score_rank`, `positive`, `negative`, `userscore`, `owners`, `average_forever`, `average_2weeks`, `median_forever`, `median_2weeks`, `price`, `initialprice`, `discount`, `ccu`;

-- ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZz

-- JOINED TABLE
DROP TABLE IF EXISTS steam_joined;
CREATE TABLE IF NOT EXISTS steam_joined (
    game_id INT,
    name STRING,
    positive INT,
    negative INT,
    owners STRING,
    ccu INT,
    release_date STRING
);

-- LOAD DATA
INSERT INTO steam_joined
SELECT t1.`steam_appid`, t2.`name`, t2.`positive`, t2.`negative`, t2.`owners`, t2.`ccu`, t1.`date`
    FROM steam_store t1
    JOIN steam_spy t2
    ON t1.`steam_appid` = t2.`appid`;
```

## Skrypty Pig

### Covid

```
lines = LOAD '/datasets/covid-dataset.jsonl'  USING JsonLoader('date:chararray, location:chararray, total_cases:chararray, new_cases:chararray, total_deaths:chararray, new_deaths:chararray, new_cases_per_million:chararray');

data = foreach lines 
       GENERATE date, 
                location,
                total_cases,
                new_cases, 
                total_deaths, 
                new_deaths, 
                (new_cases_per_million == '' ? 0 : (int)new_cases_per_million) AS new_cases_per_million;

grouped = group data all;
avg = foreach grouped generate AVG(data.new_cases_per_million);
res = foreach data generate date, location, total_cases, new_cases, total_deaths, new_deaths, new_cases_per_million, avg.$0 AS average_new_cases_per_million;
STORE res INTO '/pig_output/covid_01/' using JsonStorage();
```

### Steam

```
REGISTER 'hdfs://namenode:9000/pig_scripts/elephant-bird-pig-4.17.jar';
REGISTER 'hdfs://namenode:9000/pig_scripts/elephant-bird-core-4.17.jar';
REGISTER 'hdfs://namenode:9000/pig_scripts/elephant-bird-hadoop-compat-4.17.jar';

-- steam store
steam_store_raw = LOAD '/datasets/steam-dataset/steam_dataset/appinfo/store_data/steam_store_data.jsonl' USING com.twitter.elephantbird.pig.load.JsonLoader('-nestedLoad');
steam_store = FOREACH steam_store_raw GENERATE
    (chararray) $0#'value'#'steam_appid' AS steam_appid,
    (boolean) $0#'value'#'release_date'#'coming_soon' AS coming_soon,
    (chararray) $0#'value'#'release_date'#'date' AS date;

-- steam spy
steam_spy_raw = LOAD '/datasets/steam-dataset/steam_dataset/steamspy/basic/steam_spy_scrap.jsonl' USING com.twitter.elephantbird.pig.load.JsonLoader('-nestedLoad');
steam_spy = FOREACH steam_spy_raw GENERATE
    (chararray) $0#'value'#'appid' AS steam_appid,
    (chararray) $0#'value'#'name' AS name,
    (chararray) $0#'value'#'developer' AS developer,
    (chararray) $0#'value'#'publisher' AS publisher,
    (chararray) $0#'value'#'score_rank' AS score_rank,
    (int) $0#'value'#'positive' AS positive,
    (int) $0#'value'#'negative' AS negative,
    (int) $0#'value'#'userscore' AS userscore,
    (chararray) $0#'value'#'owners' AS owners,
    (int) $0#'value'#'average_forever' AS average_forever,
    (int) $0#'value'#'average_2weeks' AS average_2weeks,
    (int) $0#'value'#'median_forever' AS median_forever,
    (int) $0#'value'#'median_2weeks' AS median_2weeks,
    (chararray) $0#'value'#'price' AS price,
    (chararray) $0#'value'#'initialprice' AS initialprice,
    (chararray) $0#'value'#'discount' AS discount,
    (int) $0#'value'#'ccu' AS ccu;

steam_joined_raw = JOIN steam_store BY steam_appid, steam_spy BY steam_appid;
steam_joined = FOREACH steam_joined_raw GENERATE
    steam_store::steam_appid AS steam_appid,
    steam_spy::name AS name,
    steam_spy::positive AS positive,
    steam_spy::negative AS negative,
    steam_spy::owners AS owners,
    steam_spy::ccu AS ccu,
    steam_store::date AS date;

STORE steam_joined INTO '/pig_output/steam_01_combine/' using JsonStorage();
```

## Porównanie czasów wykonania

| Platforma | MapReduce |  Hive |   Pig | MapReduce |  Hive |   Pig |
|----------:|----------:|------:|------:|----------:|------:|------:|
|    Proces |     Covid | Covid | Covid |     Steam | Steam | Steam |
|    R1 [s] |    358.95 | 54.19 | 46.62 |    988.93 | 98.25 | 34.19 |
|    R2 [s] |    396.32 | 53.63 | 41.53 |    977.61 | 92.69 | 29.33 |
|    R3 [s] |    366.73 | 52.78 | 46.86 |   1001.64 | 94.82 | 34.25 |
|   Średnia |    374.00 | 53.53 | 45.00 |    989.39 | 95.25 | 32.59 |

| Proces | MapReduce [s] | Hive  [s]      | Pig [s]        |
| ------ | ------------- | -------------- | -------------- |
| Covid  | 374.00 (100%) | 53.53 (14.31%) | 45.00 (12.03%) |
| Steam  | 989.39 (100%) | 95.25 (9.62%)  | 32.59 (3.29%)  |
