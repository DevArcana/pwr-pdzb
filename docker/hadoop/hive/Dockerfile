FROM bde2020/hive:2.3.2-postgresql-metastore

RUN echo "deb http://archive.debian.org/debian stretch main" > /etc/apt/sources.list

RUN echo 'root:pass' | chpasswd
RUN apt-get -y update
RUN apt-get -y install openssh-client openssh-server
RUN echo "PermitRootLogin yes" >> /etc/ssh/sshd_config
RUN service ssh restart

RUN apt-get -y install supervisor
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# RUN /usr/bin/ssh-keygen -A

# ENTRYPOINT ["entrypoint.sh"]
# CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]