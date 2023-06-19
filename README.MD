# Hadoop + Pig + Hive + Spark 

This repo is based mostly on https://github.com/wxw-matt/docker-hadoop. It should work on AMD64 and ARM64 architectures. It's not really polished and we used a couple of hacks to solve problems, but generally it works.

Main features:
- Pig, Hive and Spark installed and preconfigured
- One central jupyter notebook to control all containers via SSH

It should work on both AMD64 and ARM64 architectures.

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

# How to run hadoop jobs

Use the jupyter notebook. It should be available on [localhost:8888](http://localhost:8888). 




