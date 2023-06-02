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


-- ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZz

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


SELECT * FROM steam_spy LIMIT 10;
SELECT * FROM steam_store LIMIT 10;


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