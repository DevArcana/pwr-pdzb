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