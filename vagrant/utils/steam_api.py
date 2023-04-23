#!/usr/bin/env python3
import sys
import requests
import pandas as pd


def get_url(game_id):
    return f'https://steamcharts.com/app/{game_id}/chart-data.json'


def download_data(ids):
    ids_dump = list()
    timestamps_dump = list()
    player_count_dump = list()
    for game_id in ids:
        r = requests.get(url=get_url(game_id)).json()
        data_temp = list(zip(*r))
        ids_dump = ids_dump + [game_id] * len(r)
        timestamps_dump = timestamps_dump + list(data_temp[0])
        player_count_dump = player_count_dump + list(data_temp[1])

    return pd.DataFrame(
        {'game_id': ids_dump,
         'timestamp': timestamps_dump,
         'player_count': player_count_dump
         })


if __name__ == '__main__':
    sys.argv[0]
    data = download_data([10, 20, 30])
    data.to_csv('games_popularity.csv', index=False, encoding='utf-8')
