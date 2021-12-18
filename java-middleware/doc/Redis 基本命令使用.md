

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
对应RedisTemplate的使用。org.springframework.data.redis.core.RedisTemplate


redisTemplate.setKeySerialize  最常用的一步，把对象存Redis，需要对象实现序列化接口Serializable


## 使用Jedis


## Redis配置使用
可以没有redis.conf文件,输出info信息为：
```text
"# Server
redis_version:6.2.1
redis_git_sha1:00000000
redis_git_dirty:0
redis_build_id:b54db1313f408a09
redis_mode:standalone
os:Linux 4.18.0-305.3.1.el8.x86_64 x86_64
arch_bits:64
multiplexing_api:epoll
atomicvar_api:c11-builtin
gcc_version:8.3.0
process_id:1
process_supervised:no
run_id:d8210f09a0181bdca803038fb241bc1130693127
tcp_port:6379
server_time_usec:1639451275783309
uptime_in_seconds:386637
uptime_in_days:4
hz:10
configured_hz:10
lru_clock:12061323
executable:/data/redis-server
config_file:
io_threads_active:0

# Clients
connected_clients:2
cluster_connections:0
maxclients:10000
client_recent_max_input_buffer:24
client_recent_max_output_buffer:0
blocked_clients:0
tracking_clients:0
clients_in_timeout_table:0

# Memory
used_memory:894800
used_memory_human:873.83K
used_memory_rss:11251712
used_memory_rss_human:10.73M
used_memory_peak:894832
used_memory_peak_human:873.86K
used_memory_peak_perc:100.00%
used_memory_overhead:851000
used_memory_startup:809992
used_memory_dataset:43800
used_memory_dataset_perc:51.65%
allocator_allocated:1059504
allocator_active:1331200
allocator_resident:3694592
total_system_memory:3918188544
total_system_memory_human:3.65G
used_memory_lua:37888
used_memory_lua_human:37.00K
used_memory_scripts:0
used_memory_scripts_human:0B
number_of_cached_scripts:0
maxmemory:0
maxmemory_human:0B
maxmemory_policy:noeviction
allocator_frag_ratio:1.26
allocator_frag_bytes:271696
allocator_rss_ratio:2.78
allocator_rss_bytes:2363392
rss_overhead_ratio:3.05
rss_overhead_bytes:7557120
mem_fragmentation_ratio:13.21
mem_fragmentation_bytes:10399672
mem_not_counted_for_evict:0
mem_replication_backlog:0
mem_clients_slaves:0
mem_clients_normal:41008
mem_aof_buffer:0
mem_allocator:jemalloc-5.1.0
active_defrag_running:0
lazyfree_pending_objects:0
lazyfreed_objects:0

# Persistence
loading:0
current_cow_size:0
current_fork_perc:0.00%
current_save_keys_processed:0
current_save_keys_total:0
rdb_changes_since_last_save:0
rdb_bgsave_in_progress:0
rdb_last_save_time:1639064638
rdb_last_bgsave_status:ok
rdb_last_bgsave_time_sec:-1
rdb_current_bgsave_time_sec:-1
rdb_last_cow_size:0
aof_enabled:0
aof_rewrite_in_progress:0
aof_rewrite_scheduled:0
aof_last_rewrite_time_sec:-1
aof_current_rewrite_time_sec:-1
aof_last_bgrewrite_status:ok
aof_last_write_status:ok
aof_last_cow_size:0
module_fork_in_progress:0
module_fork_last_cow_size:0

# Stats
total_connections_received:4
total_commands_processed:23
instantaneous_ops_per_sec:0
total_net_input_bytes:640
total_net_output_bytes:9203
instantaneous_input_kbps:0.00
instantaneous_output_kbps:0.00
rejected_connections:0
sync_full:0
sync_partial_ok:0
sync_partial_err:0
expired_keys:0
expired_stale_perc:0.00
expired_time_cap_reached_count:0
expire_cycle_cpu_milliseconds:8151
evicted_keys:0
keyspace_hits:0
keyspace_misses:0
pubsub_channels:0
pubsub_patterns:0
latest_fork_usec:0
total_forks:0
migrate_cached_sockets:0
slave_expires_tracked_keys:0
active_defrag_hits:0
active_defrag_misses:0
active_defrag_key_hits:0
active_defrag_key_misses:0
tracking_total_keys:0
tracking_total_items:0
tracking_total_prefixes:0
unexpected_error_replies:0
total_error_replies:4
dump_payload_sanitizations:0
total_reads_processed:30
total_writes_processed:26
io_threaded_reads_processed:0
io_threaded_writes_processed:0

# Replication
role:master
connected_slaves:0
master_failover_state:no-failover
master_replid:b41a66245d57b580a292788e1f99a1988ae4dee7
master_replid2:0000000000000000000000000000000000000000
master_repl_offset:0
second_repl_offset:-1
repl_backlog_active:0
repl_backlog_size:1048576
repl_backlog_first_byte_offset:0
repl_backlog_histlen:0

# CPU
used_cpu_sys:912.368141
used_cpu_user:820.705202
used_cpu_sys_children:0.004180
used_cpu_user_children:0.000588
used_cpu_sys_main_thread:912.364864
used_cpu_user_main_thread:820.704426

# Modules

# Errorstats
errorstat_ERR:count=1
errorstat_NOAUTH:count=3

# Cluster
cluster_enabled:0

# Keyspace
"
```

可以自己写redis-default.conf，用来做启动配置
```text
daemonize on

bind 0.0.0.0

protexted-mode on

tcp-backlog 511

loglevel notice

database 16

# Store
rbdcompression yes
    
rbdchecksum yes

aof-rewrite-incermental-fsync yes

rbd-save-incermental-fsync yes

# Net
repl-timeout 60

tcp-keepalive 69

timeout 300

```