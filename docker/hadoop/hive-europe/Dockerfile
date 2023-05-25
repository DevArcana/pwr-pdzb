FROM wxwmatt/hadoop-base:2.1.0-hadoop3.3.1-java8

#RUN echo "deb http://archive.debian.org/debian stretch main" > /etc/apt/sources.list

# Allow buildtime config of HIVE_VERSION
ARG HIVE_VERSION=3.1.2
ENV HIVE_VERSION=${HIVE_VERSION:-3.1.2}

ENV HIVE_HOME /opt/hive
ENV PATH $HIVE_HOME/bin:$PATH
ENV HADOOP_HOME /opt/hadoop-$HADOOP_VERSION

WORKDIR /opt

RUN apt-get update && apt-get install -y wget procps
RUN wget https://archive.apache.org/dist/hive/hive-$HIVE_VERSION/apache-hive-$HIVE_VERSION-bin.tar.gz

#Install Hive and PostgreSQL JDBC
RUN tar -xzvf apache-hive-$HIVE_VERSION-bin.tar.gz && \
	mv apache-hive-$HIVE_VERSION-bin hive && \
	rm  -f ./hive/lib/guava-19.0.jar && \
	cp ./hadoop-${HADOOP_VERSION}/share/hadoop/hdfs/lib/guava-27.0-jre.jar ./hive/lib/ && \
	apt-get clean && \
	rm -rf /var/lib/apt/lists/*

COPY postgresql-42.2.14.jar $HIVE_HOME/lib/postgresql-jdbc.jar
#Spark should be compiled with Hive to be able to use it
#hive-site.xml should be copied to $SPARK_HOME/conf folder

#Custom configuration goes here
ADD conf/hive-site.xml $HIVE_HOME/conf
ADD conf/beeline-log4j2.properties $HIVE_HOME/conf
ADD conf/hive-env.sh $HIVE_HOME/conf
ADD conf/hive-exec-log4j2.properties $HIVE_HOME/conf
ADD conf/hive-log4j2.properties $HIVE_HOME/conf
ADD conf/ivysettings.xml $HIVE_HOME/conf
ADD conf/llap-daemon-log4j2.properties $HIVE_HOME/conf

COPY startup.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/startup.sh

COPY entrypoint.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/entrypoint.sh

EXPOSE 10000
EXPOSE 10002

RUN echo 'root:pass' | chpasswd
RUN apt-get -y update
RUN apt-get -y install openssh-client openssh-server
RUN echo "PermitRootLogin yes" >> /etc/ssh/sshd_config
RUN service ssh restart

RUN apt-get -y install supervisor
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

#ENTRYPOINT ["entrypoint.sh"]

COPY startup.sh /startup.sh
COPY entrypoint.sh /entrypoint.sh

RUN chmod +x /startup.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["entrypoint.sh"]

CMD ["/usr/bin/supervisord"]