

Seesion 实现共享的方案有很多, 主要分为客户端实现和服务器端实现两种, 客户端实现主要是利用 Cookie 保存 Session 数据到客户端, 
服务器端的实现有诸如利用 Tomcat 提供的 Session 共享功能, 将 Session 保存到数据库, 或者使用 Redis 等来实现 Session 的共享, 这里记录一些了解到的常见方案

## Session与Cookie
Http协议本身是无状态的，这与Http协议本身的目的是相符的，客户端只需要简单向服务端请求即可，双方都没有必要记录彼此的行为。  
然而很快人们发现如果能按需生成一些信息可以让web变得更加有用，于是Html有了表单、页面等行为，服务端有了cookie的特性。
cookie的作用就是为了解决HTTP协议无状态的缺陷所作出的努力。至于后来出现的session机制则是又一种在客户端与服务器之间保持状态的解决方案。

Cookies是作为HTTP传输的头信息的一部分发给客户机的，所以向客户机发送Cookies的代码一般放在发送给浏览器的HTML文件的标记之前。服务器通过在HTTP的响应头中加上一行特殊的指示以提示浏览器按照指示生成相应的cookie。
客户端拿到Cookie后存到浏览器内存中，在使用时由浏览器识别资源所在范围，把cookie附在请求的http请求头上发送给服务器。是一种在客户端保持状态的方案

Session是存储于服务端的、用于记录和保持某些状态的一种会话跟踪技术。用户通过浏览器发起请求的时候，不用每次都回传所有的Cookie值了，只要回传一个key-value的键值对就可以了.
一般情况下这个key为JSESIONID，value为客户端第一次访问服务端时生成的唯一值，这个value可以标识和跟踪用户的会话信息，这个value在服务端被习惯称作sessionId。

## 1.客户端 Cookie 保存方式实现 Session 共享
客户端 Cookie 保存实现, 即在服务器端将保存在保存在服务器中 Session 中的数据写入到 Cookie 后保存到客户端 (通常为浏览器LocalStorage) 中, 
由于 Session 数据保存在客户端中, 所以每次请求都会带上这些 Session 数据, 因此即使两次请求在不同的服务器上也可以获取到 Session 数据, 以此达到 Session 共享, 

优点：可以大大减轻的服务器的压力, 服务器架构也相应变得简单  
缺点： 
1.Cookie 数据长度和 Cookie 保存的数量有限制
2.每次请求需要带额外的 Cookie 信息, 增加网络带宽
3.客户端 (如浏览器) 可能会禁用 Cookie 功能或者认为的更改 Cookie 的值, 由此带来功能上的问题。比如高版本的Chrome默认关了Cookie，导致无法用session登录。
4.信息保存在客户端会带来安全问题, 信息的加密和解密, 会带来额外开发和性能开销

下面是 Java 设置 Cookie 和获取 Cookie 的例子:
```text
// 设置 Cookie
@GetMapping(value = "/session/cookie/set/test")
public HashMap<String, Object> sessionSetTest(HttpServletRequest request, HttpServletResponse response) {
    // Session 中的数据保存到 Cookie
    Cookie cookie = new Cookie("data", "lupengwei:");

    // 表示哪些请求路径可以获取到这个 Cookie,
    // 例如 JSESSIONID 这个 cookie 的 path 为 "/", 表示所有的同一个 domain 下的所有请求都可以获取到这个 cookie
    // 使用 cookie.setDomain() 方法可以设置 domain
    // 如果没有设置, 默认设置为当前请求的 BASE_URL, 如这个例子 path=/session/cookie/set;
    // 只有 domain + path + name 全匹配才能获取到这个 cookie
    // 因此在开发时需要考虑二级域名等问题
    cookie.setPath("/");
    // 过期时间
    cookie.setMaxAge(3600 * 1000 * 24);
    // 表示仅 HTTP 请求可以获取到这个 cookie
    cookie.setHttpOnly(true);
    response.addCookie(cookie);

    // 在浏览的 cookie 中看到类似如下数据
    // data=lupengwei;
    // path=/;
    // domain=localhost;
    // Expires=Wed, 20 Feb 2019 07:18:29 GMT;
    HashMap<String, Object> resultMap = Maps.newHashMap();
    resultMap.put("sessionId", request.getSession(true).getId());
    return resultMap;
}

// 获取 Cookie
@GetMapping(value = "/session/cookie/get/test")
public JSONObject sessionGetTest(HttpServletRequest request) {
    String cookieValue = "";
    Cookie[] cookies = request.getCookies();
    for (Cookie cookie : cookies) {
        if(Objects.equals(cookie.getName(), "data")) {
            cookieValue = cookie.getValue();
        }
    }
    JSONObject resultData = new JSONObject();
    resultData.put("data", cookieValue);
    return resultData;
}
```

## 2.Tomcat 集群自带的 Session 复制功能实现 Session 共享
Tomcat 集群之间可以通过组播的方式实现集群内的 Session 共享, 由于是 WEB 容器自带的, 所有配置起来比较简单, 修改 Tomcat 容器的配置文件即可, 但是由于通过组播方式同步, 
当其中一台机器的的 Session 数据发生变化时, 会分发到其他的机器来进行同步, 会造成一定的网络和性能开销, 集群越大, 开销越大, 不能线性的扩展, 因此这种方案只适合小型网站。

## 3.使用MySQL数据库实现 Session 共享
Redis可以自己实现超时时间，MySQL就只能自己处理时间相关的逻辑了。


## 4.使用Redis缓存实现 Session 共享
目前比较主流的 Session 方式还是基于分布式缓存 Memcached 或 Redis 实现, 具体实现方式主要有 memcached-session-manager, tomcat-redis-session-manager 和 Spring Session, 前两者需要依赖 WEB 容器, 而后者不需要，记录该种实现方式。

```text
添加依赖
<dependency>
    <groupId>io.lettuce</groupId>
    <artifactId>lettuce-core</artifactId>
    <version>5.1.3.RELEASE</version>
</dependency>

<dependency>
    <groupId>org.springframework.session</groupId>
    <artifactId>spring-session-data-redis</artifactId>
</dependency>

添加配置项:
# 使用 Redis 存放 Session 信息, Spring Session 还支持 JDBC, MongoDB 等方式
spring.session.store-type=redis
```

创建 Spring Session 配置类, 除了加注解 EnableRedisHttpSession 启用功能外, 主要是配置 RedisConnectionFactory 类型的 Bean, 另外需要注意的是 Spring Boot 2.x 使用的 Redis 客户端是 Lettuce。
```text
 Session 过期时间设置为 3600 秒
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600)
public class SessionConfig {
    
    @Bean
    public LettuceConnectionFactory connectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration("127.0.0.1", 6379);
        redisStandaloneConfiguration.setPassword(RedisPassword.of("lupengwei.4585"));
        LettuceClientConfiguration lettuceClientConfiguration = LettucePoolingClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(10000))
                .build();
        return new LettuceConnectionFactory(redisStandaloneConfiguration, lettuceClientConfiguration);
    }
}

@Slf4j
@RestController
public class SessionController {

    @GetMapping("/spring/session/data/set/test")
    public ResponseEntity register(HttpServletRequest request) {
        request.getSession().setAttribute("data", "lupengwei");
        Map<String, Object> map = new HashMap<>();
        map.put("code", "0");
        map.put("message", "success");
        return new ResponseEntity(map, HttpStatus.OK);
    }

    @GetMapping("/spring/session/data/get/test")
    public ResponseEntity getSessionMessage(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        map.put("sessionId", request.getSession().getId());
        map.put("data",request.getSession().getAttribute("data")) ;
        return new ResponseEntity(map, HttpStatus.OK);
    }
}
```

参考文档：  
http://antsnote.club/2019/02/20/Java-%E5%85%B3%E4%BA%8E%E9%9B%86%E7%BE%A4%E4%B8%8B%E7%9A%84Session%E5%85%B1%E4%BA%AB/
