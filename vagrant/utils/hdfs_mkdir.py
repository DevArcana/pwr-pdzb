#!/usr/bin/env python3

import sys
import docker


def run(path, client):
    res = client.containers.get('master').exec_run(f"hdfs dfs -mkdir -p {path}")
    print(res)


if __name__ == '__main__':
    run(sys.argv[1], docker.from_env())
