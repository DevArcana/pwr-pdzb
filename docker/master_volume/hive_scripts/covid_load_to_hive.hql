use coviddb;
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