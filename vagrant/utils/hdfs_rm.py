#!/usr/bin/env python3

import sys
import docker


def run(hdfs_file_path, client):
    container = client.containers.get('master')
    res = container.exec_run(f"hdfs dfs -rm {hdfs_file_path}")
    print(res)


if __name__ == '__main__':
    run(sys.argv[1], docker.from_env())