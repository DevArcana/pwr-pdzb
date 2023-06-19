# Hadoop + Pig + Hive + Spark 

This repo is based mostly on https://github.com/wxw-matt/docker-hadoop. It should work on AMD64 and ARM64 architectures. It's not really polished and we used a couple of hacks to solve problems, but generally it works. We used for a university big data lab in group of 3 people (Piotr Krzystanek, Michał Liss, Marceli Sokólski).

Main features:
- Pig, Hive and Spark installed and preconfigured
- One central jupyter notebook to control all containers via SSH

It should work on both AMD64 and ARM64 architectures.

# Requirements
- [Docker](https://www.docker.com/)
- docker compose
  
# WSLCONFIG (Windows only)
We recommend adding .wslconfig file to your Windows user main directory. **Use at least 8GB of RAM, but we recommend using more if you can**.
```
[wsl2]
memory=8GB
```

# Directory structure

There are:
| Directory             | Description                                            |
| --------------------- | ------------------------------------------------------ |
| /docker/hadoop        | All dockerfiles, configuration files, scripts          |
| /docker/master_volume | Directory that is mounted in most important containers |
| /docker/sprawozdania  | Directory that is mounted in jupyter notebook          |


# How to start and stop 

To start all containers, navigate to /docker/hadoop directory and run:
```bash
./start_all.sh # For amd64
./start_all_arm.sh # For arm64
```

To stop all containers, navigate to /docker/hadoop directory run:
```bash
./stop_all.sh # For amd64
./stop_all_arm.sh # For arm64
```

# How to build images manually

All the dockerfiles required to build images manually are present in the /docker/hadoop directory. You can build all images used in /docker/hadoop/docker-compose.yml by running: 
```bash
docker compose build
```

But that should not be needed, as all of them are (at the moment of writing) already on dockerhub. 

# How upload files to hdfs

Run command in the namenode.

# How to run hadoop jobs (manually)

You can run commands directly in containers. Run:
- map-reduce jobs in namenode
- pig jobs in namenode
- hive jobs on hive-server
- spark jobs in namenode

Those containers have /docker/master_volume mounted, so you can paste all necessary scripts/jars there.

# How to run hadoop jobs (easy way)

Paste all necessary scripts/jars to /docker/master_volume and use jupyter notebook. It should be available on [localhost:8888](http://localhost:8888). There is a demo notebook with working examples there.
