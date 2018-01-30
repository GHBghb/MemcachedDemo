# MemcachedDemo
## emcached 的内存管理与删除机制
### 3.1: slab allocator 缓解内存碎片化
memcached 用 slab allocator 机制来管理内存.
slab allocator 原理: 预告把内存划分成数个 slab class 仓库.(每个 slab class 大小 1M) 各仓库,切分成不同尺寸的小块(chunk). 需要存内容时,判断内容的大小,为其选取合理的仓

### 3.2 系统如何选择合适的 chunk?
memcached 根据收到的数据的大小, 选择最适合数据大小的 chunk 组(slab class)。 memcached 中保存着 slab class 内空闲 chunk 的列表, 根据该列表选择空的 chunk, 然后将数 据缓存于其中。
### 3.3 固定大小 chunk 带来的内存浪费
由于 slab allocator 机制中, 分配的 chunk 的大小是”固定”的, 因此, 对于特定的 item,可能造 成内存空间的浪费.
比如, 将 100 字节的数据缓存到 122 字节的 chunk 中, 剩余的 22 字节就浪费了
对于 chunk 空间的浪费问题,无法彻底解决,只能缓解该问题.
开发者可以对网站中缓存中的 item 的长度进行统计,并制定合理的 slab class 中的 chunk 的大 小.
可惜的是,我们目前还不能自定义 chunk 的大小,但可以通过参数来调整各 slab class 中 chunk 大小的增长速度. 即增长因子, grow factor!
### 3.4 memcached 的过期数据惰性删除
1: 当某个值过期后,并没有从内存删除, 因此,stats 统计时, curr_item 有其信息 2: 当某个新值去占用他的位置时,当成空 chunk 来占用.
3: 当 get 值时,判断是否过期,如果过期,返回空,并且清空, curr_item 就减少了.
即--这个过期,只是让用户看不到这个数据而已,并没有在过期的瞬间立即从内存删除. 这个称为 lazy expiration, 惰性失效.
好处--- 节省了 cpu 时间和检测的成本
### 3.5: memcached 此处用的 lru 删除机制.
如果以 122byte 大小的 chunk 举例, 122 的 chunk 都满了, 又有新的值(长度为 120)要加入, 要 挤掉谁?
memcached 此处用的 lru 删除机制.
(操作系统的内存管理,常用 fifo,lru 删除)
lru: least recently used 最近最少使用 fifo: first in ,first out
原理: 当某个单元被请求时,维护一个计数器,通过计数器来判断最近谁最少被使用. 就把谁t出.
### 3.6 memcached 中的一些参数限制
key 的长度: 250 字节, (二进制协议支持 65536 个字节)
value 的限制: 1m, 一般都是存储一些文本,如新闻列表等等,这个值足够了. 内存的限制: 32 位下最大设置到 2g.
如果有 30g 数据要缓存,一般也不会单实例装 30g, (不要把鸡蛋装在一个篮子里), 一般建议 开启多个实例(可以在不同的机器,或同台机器上的不同端口)
      注: 即使某个 key 是设置的永久有效期,也一样会被踢出来! 即--永久数据被踢现象
