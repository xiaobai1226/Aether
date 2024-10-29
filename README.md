# 1、简介

- Aether是一个开源的网盘项目
- 技术栈：Solon、MybatisPlus、Vue3

# 2、安装

使用docker-compose安装

``` yaml
version: "3"

services:
  aether:
    image: xiaobai1226/aether:0.2.0
    container_name: aether
    restart: always
    depends_on:
      aetherdb:
        condition: service_healthy
      aetherredis:
        condition: service_healthy
    volumes:
      - $PWD/aether/data:/root/aether/data
      - $PWD/aether/config:/root/aether/config
      - $PWD/aether/logs:/root/aether/logs
      - /etc/localtime:/etc/localtime:ro
    ports:
      - "2333:2333"
  aetherdb:
    image: mysql:9.0.1
    container_name: aetherdb
    restart: always
    volumes:
      - $PWD/mysql/data:/var/lib/mysql
      - $PWD/mysql/conf/my.cnf:/etc/my.cnf
      - $PWD/mysql/logs:/var/logs
      - /etc/localtime:/etc/localtime:ro
    environment:
      MYSQL_ROOT_PASSWORD: xxxxxxxx
      MYSQL_DATABASE: aether
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "127.0.0.1", "--silent"]
      interval: 10s
      retries: 5
      timeout: 5s
      start_period: 120s
  aetherredis:
    restart: always
    image: redis:7.4.0
    container_name: aetherredis
    volumes:
      - $PWD/redis/data:/data
      - $PWD/redis/conf/redis.conf:/etc/redis/redis.conf
      - /etc/localtime:/etc/localtime:ro
    command: redis-server /etc/redis/redis.conf
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 10s
      retries: 3
      start_period: 30s
```


