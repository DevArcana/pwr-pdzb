import docker
client = docker.from_env()
container = client.containers.get('master')

def hdfs_mkdir(path):
    container.exec_run(f"hdfs dfs -mkdir -p /{path}/")

def hdfs_upload(path):
    directory = "/".join(path.split("/")[:-1])
    hdfs_mkdir(directory)
    container.exec_run(f"hdfs dfs -put /data/master_volume/{path} /{directory}")

def hdfs_set_replication_level(number):
    container.exec_run(f"hdfs dfs -setrep -R {number} /")

#hdfs_upload("covid.csv")
#hdfs_upload("steam_dataset")
hdfs_set_replication_level(4)