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
