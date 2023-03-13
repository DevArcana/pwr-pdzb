rm -rf datasets
mkdir datasets
curl https://covid.ourworldindata.org/data/owid-covid-data.csv --output datasets/covid.csv
date > datasets/downloaded_at.txt