#!/usr/bin/env python3

import sys
import docker
import os
import tarfile


def run(local_file_path, hdfs_path, client):
    def copy_to(src, dst, client):
        name, dst = dst.split(':')
        container = client.containers.get(name)
        src = os.path.abspath(src)
        os.chdir(os.path.dirname(src))
        srcname = os.path.basename(src)
        tar = tarfile.open(f"{src}.tar", mode='w')
        try:
            tar.add(srcname)
        finally:
            tar.close()
        data = open(f"{src}.tar", 'rb').read()
        container.put_archive(os.path.dirname(dst), data)
        os.remove(f"{src}.tar")
        return srcname

    local_file_path = sys.argv[1]
    hdfs_path = sys.argv[2]

    name = copy_to(local_file_path, "master:/home/temp/", client)
    container = client.containers.get('master')
    res = container.exec_run(f"hdfs dfs -put /home/temp/{name} {hdfs_path}")
    print(res)


if __name__ == '__main__':
    run(sys.argv[1], sys.argv[2], docker.from_env().containers.get('master'))