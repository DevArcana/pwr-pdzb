FROM wxwmatt/hadoop-base:2.1.1-hadoop3.3.1-java8

# RUN apt-get update
# RUN DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
#       build-essential \
#       iputils-ping \
#       wget

# RUN DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
#       python3.7-dev \
#       python3.7 \
#       python3-pip

# RUN update-alternatives --install /usr/bin/python python /usr/bin/python3 1

# RUN pip3 install wheel
# RUN pip3 install setuptools
# RUN pip3 install py4j
# RUN pip3 install pyspark
# RUN pip3 install numpy
# RUN pip3 install pandas

# RUN DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
#       gcc \
#       git-core \
#       libffi-dev

# RUN pip3 install notebook

# RUN DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
#       libjpeg-dev \
#       libpcre3 \
#       libpcre3-dev \
#       autoconf \
#       libtool \
#       pkg-config\
#       zlib1g-dev \
#       libssl-dev \
#       libexpat1-dev \
#       libxslt1.1\
#       gnuplot

# RUN pip3 install matplotlib

HEALTHCHECK CMD curl -f http://localhost:9870/ || exit 1

ENV HDFS_CONF_dfs_namenode_name_dir=file:///hadoop/dfs/name
RUN mkdir -p /hadoop/dfs/name
VOLUME /hadoop/dfs/name

ADD run.sh /run.sh
RUN chmod a+x /run.sh

RUN echo 'root:pass' | chpasswd
RUN apt-get -y update
RUN apt-get -y install openssh-client openssh-server
RUN echo "PermitRootLogin yes" >> /etc/ssh/sshd_config
RUN service ssh restart

RUN apt-get -y install wget
RUN wget https://downloads.apache.org/pig/pig-0.17.0/pig-0.17.0.tar.gz
RUN wget https://dlcdn.apache.org/spark/spark-3.4.0/spark-3.4.0-bin-hadoop3-scala2.13.tgz

RUN apt-get -y install supervisor
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

RUN tar -xf pig-0.17.0.tar.gz
RUN cd pig-0.17.0
RUN mv pig-0.17.0 /usr/local/pig
RUN rm pig-0.17.0.tar.gz
ENV PATH="${PATH}:/usr/local/pig/bin"

RUN tar -xzf spark-3.4.0-bin-hadoop3-scala2.13.tgz
RUN mv spark-3.4.0-bin-hadoop3-scala2.13 /usr/local/spark
RUN rm spark-3.4.0-bin-hadoop3-scala2.13.tgz
ENV SPARK_HOME=/usr/local/spark
ENV PATH="${PATH}:${SPARK_HOME}/bin"

RUN apt-get -y install python3-pip

RUN pip3 install pyspark

ADD run_spark_history_server.sh /run_spark_history_server.sh
RUN chmod a+x /run_spark_history_server.sh

ADD spark-defaults.conf /usr/local/spark/conf/spark-defaults.conf

EXPOSE 9870
CMD ["/usr/bin/supervisord"]