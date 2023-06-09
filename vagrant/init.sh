apt update

# Setup docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh
sudo curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
sudo groupadd docker
sudo usermod -aG docker vagrant
newgrp docker

# Setup python
sudo apt update
sudo apt install software-properties-common -y
sudo add-apt-repository ppa:deadsnakes/ppa -y
sudo apt install python3.11 -y
sudo apt install python3.11-dev -y
sudo apt install python3.11-distutils -y
sudo apt install python3.11-venv -y
curl -sS https://bootstrap.pypa.io/get-pip.py | python3.11
echo 'export PATH=/home/${USER}/.local/bin:$PATH' >> ~/.profile
source ~/.profile
pip install --upgrade pip
sudo apt install python-is-python3 -y
sudo update-alternatives --install /usr/bin/python python /usr/bin/python3.11 1
pip install docker
pip install --upgrade jinja2
pip install -r /vagrant/sprawozdania/akwizycja/requirements.txt
pip install notebook

# Start jupyter
echo "Setup Jupyter auto start"
cat >/etc/systemd/system/jupyter.service <<EOL
[Unit]
Description=Jupyter Workplace
[Service]
Type=simple
PIDFile=/run/jupyter.pid
ExecStart=jupyter notebook --port=8888 --no-browser --ip=0.0.0.0 --NotebookApp.token= --NotebookApp.password='' --notebook-dir=/vagrant/sprawozdania
User=vagrant
Group=vagrant
WorkingDirectory=/vagrant/sprawozdania
Restart=always
RestartSec=10
[Install]
WantedBy=multi-user.target
EOL

systemctl enable jupyter.service
systemctl daemon-reload
systemctl restart jupyter.service

# Start hadoop
cd /vagrant/scripts
./start_containers.sh
./hadoop-start.sh start > /dev/null 2>&1 &