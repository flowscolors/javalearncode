

## Redis 命令行使用

```shell script
set college szu

hset person name bingo
hset person age 20
hset person id 1
hget person name
person = {
    "name": "bingo",
    "age": 20,
    "id": 1
}

# 0开始位置，-1结束位置，结束位置为-1时，表示列表的最后一个位置，即查看所有。
lrange mylist 0 -1

lpush mylist 1
lpush mylist 2
lpush mylist 3 4 5

# 1
rpop mylist

#-------操作一个set-------
# 添加元素
sadd mySet 1

# 查看全部元素
smembers mySet

# 判断是否包含某个值
sismember mySet 3

# 删除某个/些元素
srem mySet 1
srem mySet 2 4

# 查看元素个数
scard mySet

# 随机删除一个元素
spop mySet

#-------操作多个set-------
# 将一个set的元素移动到另外一个set
smove yourSet mySet 2

# 求两set的交集
sinter yourSet mySet

# 求两set的并集
sunion yourSet mySet

# 求在yourSet中而不在mySet中的元素
sdiff yourSet mySet

#-------sortedset-------
zadd board 85 zhangsan
zadd board 72 lisi
zadd board 96 wangwu
zadd board 63 zhaoliu

# 获取排名前三的用户（默认是升序，所以需要 rev 改为降序）
zrevrange board 0 3

# 获取某用户的排名
zrank board zhaoliu
```



## Redis Java程序使用
一般是要在Java客户端使用上再封装一层Util工具类。
### 使用Lettue 
对应RedisTemplate的使用。

org.springframework.data.redis.core.RedisTemplate
```
redisTemplate.setKeySerialize
```


## Redis配置使用
redis-default.conf
```text
daemonize on
bind 0.0.0.0
protexted-mode on
tcp-backlog 511

```