docker compose up -d

# docker exec -u 0 namenode bash -c "echo 'root:pass' | chpasswd"
# docker exec -u 0 namenode bash -c "apt-get -y update"
# docker exec -u 0 namenode bash -c "apt-get -y install openssh-client openssh-server"
# docker exec -u 0 namenode bash -c "echo 'PermitRootLogin yes' >> /etc/ssh/sshd_config"
# docker exec -u 0 namenode bash -c "service ssh restart"
# docker exec -u 0 namenode bash -c "export -p >env_var.sh"

#docker exec -u 0 jupyter bash -c "sudo gem install apt-spy2"
#docker exec -u 0 jupyter bash -c "sudo apt-spy2 fix --commit --launchpad --country=PL"
#docker exec -u 0 jupyter bash -c "apt-get -y --option="APT::Acquire::Retries=3" update"
#docker exec -u 0 jupyter bash -c "apt-get -y --option="APT::Acquire::Retries=3" install openssh-client openssh-server"
# docker exec -u 0 jupyter bash -c "pip install docker"
# docker exec -u 0 jupyter bash -c "pip install json"
# docker exec -u 0 jupyter bash -c "pip install csv"
# docker exec -u 0 jupyter bash -c "pip install pandas"
# docker exec -u 0 jupyter bash -c "pip install paramiko"
# docker exec -u 0 jupyter bash -c "pip install opendatasets"