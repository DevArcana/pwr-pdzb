# Sprawozdanie 6 - akwizycja danych

## Środowisko
Mamy maszynę wirtualną z Ubuntu postawioną za pomocą Vagrant'a (korzystającego pod spodem z VirtualBox'a). Na tej maszynie wirtualnej stawiamy kontenery Docker'a. 

Aby ułatwić sobie późniejszą pracę z obrazami na których postawiony jest hadoop postanowiliśmy dodać do master-node volumen na dane (modyfikując skrypty generujące docker-compose). Dzięki temu możemy w wygodny sposób (tj. poprzez wrzucenie do odpowiedniego folderu) przenosić pliki do miejsca, do którego możemy się dostać z poziomu maszyny z hadoopem. Warto zwrócić uwagę, że maszyna wirtualna także posiada taki wolumen, który zapewnia wykorzystanie Vagrant'a.

```yaml
master:
    image: hjben/hadoop-eco:$hadoop_version
    hostname: master
    container_name: master
    privileged: true
    ports:
      - 8088:8088
      - 9870:9870
      - 8042:8042
      - 10000:10000
      - 10002:10002
      - 16010:16010
    volumes:
      - /sys/fs/cgroup:/sys/fs/cgroup
      - $hdfs_path:/data/hadoop
      - $hadoop_log_path:/usr/local/hadoop/logs
      - $hbase_log_path/master:/usr/local/hbase/logs
      - $hive_log_path:/usr/local/hive/logs
      - $sqoop_log_path:/usr/local/sqoop/logs
      - /vagrant/master_volume:/data/master_volume <-------------- dodany volumen
    networks:
      hadoop-cluster:
        ipv4_address: 10.1.2.3
    extra_hosts:
      - "mariadb:10.1.2.2"
      - "master:10.1.2.3"
```

## Pobieranie danych
Niestety przez potrzebę generowania i podania klucza API do serwisu kaggle przed pobraniem danych należy wykonać kilka czynności.

1. Pobrać ze strony kaggle klucz API (kaggle.json)
2. Stworzyć folder .kaggle w głównym katalogu użytkownika i skopiować tam klucz API
3. Wywołać nasz skrypt setup.sh, który:
    1. Przygotowuje środowisko pythonowe
    2. Pobiera z kaggle:
       * [YouTube Trending Video Dataset](https://www.kaggle.com/datasets/rsrishav/youtube-trending-video-dataset)
       * [Steam Dataset](https://www.kaggle.com/datasets/souyama/steam-dataset)
    3. Pobiera z sieci [dane Covid'owe](https://covid.ourworldindata.org/data/owid-covid-data.csv)

W tym momencie na dysku powinny znajdować się spakowane pliki z danymi

## Formatowanie danych
Po rozpakowaniu danych widać, że część z nich ma format trudny do późniejszej pracy. Ostatecznie postanowiliśmy przed wrzuceniem plików do hdfs wszystkie przetransformować do dormatu .jsonl. Format .jsonl zawiera obiekty json, każdy w kolejnej linii. Dzięki zastosowaniu takiego formatu na kolejnych laboratoriach będzie można wykorzystywać odpowiednie mappery.

W tym momencie mamy pliki z danymi w formacie .jsonl (+ covid.csv) Pliki te są umieszczone na volumenie, który widzi kontener z hdoop'em.

## Dodanie plików do hdfs

Stworzyliśmy skrypt w języku python, który dodaje wszystkie pliki .jsonl do hdfs. Skrypt ten musi być uruchomiony z wirtualnej maszyny z Ubuntu. Skrypt ten ustawia liczbę replik danych na 4.

```py
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

hdfs_upload("covid.csv")
hdfs_upload("steam_dataset")
hdfs_upload("test.txt")
hdfs_set_replication_level(4)
```

W tym momencie mamy dane, które uznajemy jako niezmienne w hdfs

![](images/DaneWhdfsConsole.png)
![](images/DaneWhdfsClient.png)

## Akwizycja danych zmiennych

Nasz proces zakłada, że na podstawie części danych niezmiennych dotyczących gier z serwisu Steam (steam_dataset) wytypujemy te gry, o których liczby graczy na przestrzeni czasu będziemy pytać SteamCharts API za pomocą bardzo prostego zapytania
```
https://steamcharts.com/app/<appid>/chart-data.json
```

W celu akwizycji tych danych przygotowaliśmy proces map-reduce, który przyjmuje na wejściu potrzebne id i zwraca wyniki zapytania w odpowiedniej postaci. Na potrzeby tej listy zadań umieściliśmy w hdfs odpowiedni plik z wybranymi przez nas identyfikatorami do celów testowych. W przyszłości ten krok będzie wykorzystywał wyniki z poprzednich podprocesów.

![](images/SteamCharts.png)

```scala
```

![](images/DaneWhdfsSteam.png)

## Testowe map-reduce

Do każdego pliku przygotowaliśmy testowy map-reduce zliczający konkretną wartość, aby sprawdzić, czy pliki mają poprawny, możliwy do przetworzenia format