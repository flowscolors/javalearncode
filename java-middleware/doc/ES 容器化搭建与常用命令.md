## 1.容器化安装

```
docker pull elasticsearch:7.10.1
mkdir -p /data/elasticsearch/data 
mkdir -p /data/elasticsearch/config 
mkdir -p /data/elasticsearch/plugins
chmod -R 777 /data/elasticsearch

docker-compose up -d
docker ps -a | grep elasticsearch 
curl http://127.0.0.1:9200/
```

docker-compose.yml

``` yaml

version: '2'
services:
  elasticsearch:
    container_name: elasticsearch
    image: elasticsearch:7.10.1
    ports:
      - "9200:9200"
    volumes:
      - /data/elasticsearch/config/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml
      - /data/elasticsearch/data:/usr/share/elasticsearch/data
      - /data/elasticsearch/plugins:/usr/share/elasticsearch/plugins
    environment:
      - "ES_JAVA_OPTS=-Xms64m -Xmx512m"
      - "discovery.type=single-node"
      - "COMPOSE_PROJECT_NAME=elasticsearch-server"
    restart: always
```

/data/elasticsearch/config/elasticsearch.yml
```yaml
cluster.name: "docker-cluster"
network.host: 0.0.0.0

```
参考文档：  
https://segmentfault.com/a/1190000039238109

## 常用命令



## 常用Java客户端命令

