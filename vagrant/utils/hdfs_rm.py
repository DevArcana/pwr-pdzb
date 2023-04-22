import sys
import docker

if __name__ == '__main__':
    hdfs_file_path = sys.argv[1]

    client = docker.from_env()
    container = client.containers.get('master')
    res = container.exec_run(f"hdfs dfs -rm {hdfs_file_path}")
    print(res)