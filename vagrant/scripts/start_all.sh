./start_containers.sh
./hadoop-start.sh start > /dev/null 2>&1 &
docker exec -it master bash