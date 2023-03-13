export $(cat .env | xargs)
source venv/bin/activate

rm -rf datasets
mkdir datasets
cd datasets

curl https://covid.ourworldindata.org/data/owid-covid-data.csv --output covid.csv
kaggle datasets download -d souyama/steam-dataset
kaggle datasets download -d rsrishav/youtube-trending-video-dataset

date > downloaded_at.txt