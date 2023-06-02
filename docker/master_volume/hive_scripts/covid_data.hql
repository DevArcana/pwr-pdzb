use coviddb;
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
    new_cases_per_million + 1.1
  FROM transformed_data
) t;

