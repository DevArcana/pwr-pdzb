docker exec -u 0 namenode bash -c "echo 'root:pass' | chpasswd"
docker exec -u 0 namenode bash -c "apt-get -y update"
docker exec -u 0 namenode bash -c "apt-get -y install openssh-client openssh-server"
docker exec -u 0 namenode bash -c "echo 'PermitRootLogin yes' >> /etc/ssh/sshd_config"
docker exec -u 0 namenode bash -c "service ssh restart"
docker exec -u 0 namenode bash -c "export -p >env_var.sh"

docker exec -u 0 jupyter bash -c "apt-get -y update"
docker exec -u 0 jupyter bash -c "apt-get -y install openssh-client openssh-server"
