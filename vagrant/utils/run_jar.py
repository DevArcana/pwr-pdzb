#!/usr/bin/env python3

import docker
import os
import tarfile
import sys

client = docker.from_env()
filePath = sys.argv[1]
params = sys.argv[2:]

def copy_to(src, dst):
    name, dst = dst.split(':')
    container = client.containers.get(name)
    src = os.path.abspath(src)
    os.chdir(os.path.dirname(src))
    srcname = os.path.basename(src)
    tar = tarfile.open(src + '.tar', mode='w')
    try:
        tar.add(srcname)
    finally:
        tar.close()
    data = open(src + '.tar', 'rb').read()
    container.put_archive(os.path.dirname(dst), data)


copy_to(filePath, "master:/home/test.jar")

container = client.containers.get('master')
res = container.exec_run("yarn jar /home/test.jar " + " ".join(params))
print(res)
