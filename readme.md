###      

###  Spring Cloud Demo学习

从[How2J]学习

[How2J]: https://how2j.cn/k/springcloud/springcloud-distribution/2037.html	"Spring Cloud学习"

PS 注意事项：启动服务必须是按照顺序来。且版本问题暂定为Spring Boot 2.0.3

SpringCloud 就是一套工具，帮助大家很容易地搭建出这么一个 
集群和分布式的架子出来。

------

**一个 springboot 就是一个 微服务**，
并且这个 springboot 做的事情很单纯。 
比如 product-service 这个项目，
就可以拆成两个微服务，分别是 数据微服务，
和视图微服务，其实就是俩 springboot。
微服务就是多个Spring Boot的组合。
每个Spring Boot各自做各自的事情。

------

微服务注册中心：Eureka sever

在业务逻辑上， 视图微服务 需要 数据微服务 的数据，
所以就存在一个微服务访问另一个微服务的需要。
而这俩微服务已经被注册中心管理起来了，
所以 视图微服务 就可以通过 注册中心定位并访问 数据微服务了。

(分布式的雏形)

分布式的好处：

1. 如果我要更新数据微服务，视图微服务是不受影响的
2. 可以让不同的团队开发不同的微服务，他们之间只要约定好接口，彼此之间是低耦合的
3.  如果视图微服务挂了，数据微服务依然可以继续使用

![分布式概念](https://stepimagewm.how2j.cn/9320.png)

集群：

原来数据微服务只有这一个springboot, 
现在做同样数据微服务，有两个 springboot, 
他们提供的功能相同，仅端口不同，这就形成了集群。
集群好处：

1. 比起一个 springboot, 两个springboot 
可以分别部署在两个不同的机器上，那么理论上来说，
能够承受的负载就是 x 2. 这样系统就具备通过横向扩展而提高性能的机制。
2. 如果 8001 挂了，还有 8002 继续提供微服务，这就叫做高可用 。

分布式和集群周边服务：

1. 哪些微服务是如何彼此调用的？ **sleuth 服务链路追踪**
2. 如何在微服务间共享配置信息？**配置服务 Config Server**
3. 如何让配置信息在多个微服务之间自动刷新？ **RabbitMQ 总线 Bus**
4. 如果数据微服务集群都不能使用了， 视图微服务如何去处理? **断路器 Hystrix**
5. 视图微服务的断路器什么时候开启了？什么时候关闭了？ **断路器监控 Hystrix Dashboard**
6. 如果视图微服务本身是个集群，那么如何进行对他们进行聚合监控？ **断路器聚合监控 Turbine Hystrix Dashboard**
7. 如何不暴露微服务名称，并提供服务？ **Zuul 网关**

------

### 父子项目

 springcloud 比较特别，它由多个微服务组成， 微服务，指springboot。
所以可以说 springcloud 由多个 springboot 项目组成， 而这些 springboot 之间又是围绕一个共同目的而存在的。
所以，为了便于组织这些 springboot 项目，基于maven 父子-聚合 项目的方式来开发较容易上手。

------

### Spring Cloud ——Eureka

Eureka子项目pom.xml ，增加 
spring-cloud-starter-netflix-eureka-server jar 包

EurekaServer 启动类。
这是一个 EurekaServer ，它扮演的角色是注册中心，
用于注册各种微服务，以便于其他微服务找到和访问。
 所以 **Eureka** 这个单词是 “**找到啦**” 的意思。
EurekaServer 本身就是个 Springboot 微服务, 
所以它有 @SpringBootApplication 注解。
@EnableEurekaServer 表示这是个 EurekaServer  Eureka服务端。
启动的时候，端口号没有在配置文件里，而是直接放在代码里，也可写在配置文件里面

EurekaServerApplication为Eureka启动类

配置文件，提供 eureka 的相关信息。
hostname: localhost 表示主机名称。
registerWithEureka：false. 表示是否注册到服务器。 
因为它本身就是服务器，所以就无需把自己注册到服务器了。
fetchRegistry: false. 表示是否获取服务器的注册信息，
和上面同理，这里也设置为 false。
defaultZone： 
http://${eureka.instance.hostname}:${server.port}/eureka/ 自己作为服务器，公布出来的地址。 比如后续某个微服务要把自己注册到 eureka server, 那么就要使用这个地址： http://localhost:8761/eureka/

name: eurka-server 表示这个微服务本身的名称是 eureka-server

```yaml
eureka:
  instance:
    hostname: localhost
  client:
    registerWithEureka: false
    fetchRegistry: false
    serviceUrl:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
 
spring:
  application:
    name: eureka-server
```

默认端口是8761

http://127.0.0.1:8761/ 这是本地访问地址

![启动并访问](https://stepimagewm.how2j.cn/9332.png)

以上则为服务注册中心运行成功

创建子项目 product-data-service(生产者 数据 服务)会与消费者服务对接

```xml
<dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>     
    </dependencies>
```

导入jar 依赖。表示为eureka的客户端

设置yaml配置文件

```yaml
#   server:
#   port: 因为会启动多个 product-data-service, 所以端口号由用户自动设置，推荐 8001,8002,8003
 
spring:
  application:
    name: product-data-service
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

在product-data-service启动两次，分别为不同的端口。  （列如8012，8013）

可在8761端口看见两个实例

访问微服务地址可得到数据

------

### Spring Cloud ——Ribbon视图服务

springcloud 提供了两种方式，一种是 Ribbon，一种是 Feign。
Ribbon 是使用 restTemplate 进行调用，并进行客户端负载均衡。
什么是客户端负载均衡呢？ 
在前面 [注册数据微服务](https://how2j.cn/k/springcloud/springcloud-eureka-client/2039.html) 里，注册了8001和8002两个微服务， Ribbon 会从注册中心获知这个信息，然后由 Ribbon 这个客户端自己决定是调用哪个，这个就叫做客户端负载均衡。

 Feign 是对 Ribbon的封装，调用起来更简单

创建Ribbon服务项目，并导入jar依赖包

```xml

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
 
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
      </dependency>
         
    </dependencies>
```

ProductViewServiceRibbonApplication为Ribbon服务的启动类

Ribbon配置文件application.yml

```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
spring:
  application:
    name: product-view-service-ribbon
  thymeleaf:
    cache: false
    prefix: classpath:/templates/
    suffix: .html
    encoding: UTF-8
    content-type: text/html
    mode: HTML5
```

启动并访问

![调用图](https://stepimagewm.how2j.cn/9357.png)

逻辑 如图

------

### Spring Cloud ——Feign视图服务

ProductViewServiceFeignApplication为Feign启动类

创建Feign微服务并导入jar依赖包

```xml
<dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
                 
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
 
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
      </dependency>
   
    </dependencies> 
```

配置Feign服务

```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
spring:
  application:
    name: product-view-service-feign
  thymeleaf:
    cache: false
    prefix: classpath:/templates/
    suffix: .html
    encoding: UTF-8
    content-type: text/html
    mode: HTML5 
```

逻辑与Ribbon相同（本就基于Ribbon：Feign是对Ribbon封装）

------

### Spring Cloud ——服务链路追踪

在前面的例子里，我们有两个微服务，
分别是数据服务和视图服务，随着业务的增加，
就会有越来越多的微服务存在，他们之间也会有更加复杂的调用关系。
这个调用关系，仅仅通过观察代码，
会越来越难以识别，所以就需要通过 zipkin 服务链路追踪服务器 这个东西来用

需要启动链路追踪服务器，这个启动办法是下载 zipkin-server-2.10.1-exec.jar

并执行

```sh
java -jar zipkin-server-2.10.1-exec.jar

```

访问地址： http://localhost:9411/zipkin/dependency/

需要对data和view服务进行修改

都需要增加jar包

```xml
<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-starter-zipkin</artifactId>
	</dependency>     
```

并修改yaml文件的配置信息

```yaml
#   server:
#   port: 因为会启动多个 product-data-service, 所以端口号由用户自动设置，推荐 8001,8002,8003
 
spring:
  application:
    name: product-data-service
  zipkin:
    base-url: http://localhost:9411       
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

还需要再启动类中配置Sampler抽样策略



```java
@Bean
    public Sampler defaultSampler() {
        return Sampler.ALWAYS_SAMPLE;
    }   
```

------

### Spring Cloud ——配置服务器Config

有时候，微服务要做集群，这就意味着，
会有多个微服务实例。 在业务上有时候需要修改一些配置信息，
比如说 版本信息 倘若没有配置服务， 那么就需要挨个修改微服务，
挨个重新部署微服务，这样就比较麻烦。
为了偷懒， 这些配置信息就会放在一个公共的地方，
比如git, 然后通过配置服务器把它获取下来，
然后微服务再从配置服务器上取下来。
这样只要修改git上的信息，
那么同一个集群里的所有微服务都立即获取相应信息了，
这样就大大节约了开发，上线和重新部署的时间了。

如图所示，我们先在 git 里保存 version 信息， 
然后通过 ConfigServer 去获取 version 信息， 
接着不同的视图微服务实例再去 ConfigServer 里获取 version.

首先创建 ConfigServer

![配置服务的需要](https://stepimagewm.how2j.cn/9316.png)



准备好一个Git仓库

创建Config微服务并导入jar包

```xml
<dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-config-server</artifactId>
        </dependency>
 
    </dependencies> 
```

ConfigServerApplication为Config服务的启动类

配置application.yml文件

```yaml
spring:
  application:
    name: config-server
  cloud:
    config:
      label: master
      server:
        git:
          uri: https://github.com/how2j/springcloudConfig/
          searchPaths: respo
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

启动即可看见

------

### Spring Cloud ——配置客户端（修改Feign服务）

现成的 [视图微服务-Feign](https://how2j.cn/k/springcloud/springcloud-feign/2041.html) 改造成配置客户端，使得其可以从配置服务器上获取版本信息。

增加一个jar 依赖包 在Feign服务的pom.xml文件中

```xml
<dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-config</artifactId>
    </dependency>   
```

配置客户端，比较特别~。它需要在 bootstrap.yml
里配置 config-server 的信息，
而不是像以前那样在 application.yml 里进行配置。
bootstrap.yml 和 application.yml 的区别，
简单说就是前者先启动，并且一些系统方面的配置需要在 bootstrap.yml 里进行配置。
更多关于他们的区别，请自行百度。
在 bootstrap.yml 里配置提供了 serviceId: config-server, 
这个是配置服务器在 eureka server 里的服务名称，
这样就可以定位 config-server了。

增加bootstrap.yml配置文件

```yaml
spring:
  cloud:
    config:
      label: master
      profile: dev
      discovery:
        enabled:  true
        serviceId:  config-server
  client:
    serviceUrl:
      defaultZone:  http://localhost:8761/eureka/
```

修改application.yaml文件。
把eureka地址信息移动到了bootstrap.yml中(修改如下)

```yaml
spring:
  application:
    name:  product-view-service-feign
  thymeleaf:
    cache: false
    prefix: classpath:/templates/
    suffix: .html
    encoding: UTF-8
    content-type: text/html
    mode: HTML5       
  zipkin:
    base-url: http://localhost:9411 
```

即可从git仓库修改信息并同步到本地

------

### Spring Cloud ——消息总线Bus（修改Feign服务）

之前的教程虽然配置了config-server, 
也把 视图服务改造成了配置客户端，但是当需要刷新配置信息的时候，
不得不既重启 config-server, 又重启微服务。 这样的体验当然是不太好的。 
我们当然是希望一旦 git 上的配置信息修改之后，就可以自动地刷新到微服务里，
而不是需要手动重启才可以。

消息总线Bus及为此

以RabbitMQ来进行消息广播，以达到有配置信息发生改变的时候，
广播给多个微服务的效果。
所以需要先安装 rabbitMQ 服务器。

pom.xml文件添加jar包

```xml
<dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>   
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-bus-amqp</artifactId>
    </dependency> 
```

bootstrap.yml问价新增bus总线配置以及RabbitMQ配置

```yaml
spring:
  cloud:
    config:
      label: master
      profile: dev
      discovery:
        enabled:  true
        serviceId:  config-server
    bus:
      enabled: true
      trace:
        enabled: true
  client:
    serviceUrl:
      defaultZone:  http://localhost:8761/eureka/
   
rabbitmq:
  host: localhost
  port: 5672
  username: guest
  password: guest 
```

application.yml文件新增路径访问允许

```yaml
spring:
  application:
    name:  product-view-service-feign
  thymeleaf:
    cache: false
    prefix: classpath:/templates/
    suffix: .html
    encoding: UTF-8
    content-type: text/html
    mode: HTML5       
  zipkin:
    base-url: http://localhost:9411   
     
management:
  endpoints:
    web:
      exposure:
        include: "*"
      cors:
        allowed-origins: "*"
        allowed-methods: "*"    
```

ProductDataServiceApplication启动类新增RabbitMQ端口检测

```java
//判断 rabiitMQ 是否启动
        int rabbitMQPort = 5672;
        if(NetUtil.isUsableLocalPort(rabbitMQPort)) {
            System.err.printf("未在端口%d 发现 rabbitMQ服务，请检查rabbitMQ 是否启动", rabbitMQPort );
            System.exit(1);
        }     
```

使用 post 的方式访问 http://localhost:8012/actuator/bus-refresh 地址，
之所以要专门做一个 FreshConfigUtil 类，就是为了可以使用 post 访问，
因为它不支持 get 方式访问，直接把这个地址放在浏览器里，是会抛出 405错误的。

```java
public class FreshConfigUtil {
 
    public static void main(String[] args) {
        HashMap<String,String> headers =new HashMap<>();
        headers.put("Content-Type", "application/json; charset=utf-8");
        System.out.println("因为要去git获取，还要刷新config-server, 会比较卡，所以一般会要好几秒才能完成，请耐心等待");
 
        String result = HttpUtil.createPost("http://localhost:8012/actuator/bus-refresh").addHeaders(headers).execute().body();
        System.out.println("result:"+result);
        System.out.println("refresh 完成");
    }
}
```

对服务链路追踪的影响

因为视图服务进行了改造，支持了 rabbitMQ, 那么在默认情况下，
它的信息就不会进入 Zipkin了。 在Zipkin 里看不到视图服务的资料了。
为了解决这个问题，在启动 Zipkin 的时候 
带一个参数就好了：--zipkin.collector.rabbitmq.addresses=localhost
即本来是

```powershell
java -jar zipkin-server-2.10.1-exec.jar
```

改成

```shell
java -jar zipkin-server-2.10.1-exec.jar --zipkin.collector.rabbitmq.addresses=localhost
```

------

### Spring Cloud ——断路器Hystrix（修改Feign服务）

我们知道，视图微服务是依赖于数据微服务的。
那么当数据微服务不可用的时候，会报错，状态为500。
我们主动把 ProductDataServiceApplication 关闭，然后再访问 ：
http://localhost:8012/products 就会抛出如图所示的异常。

![问题](https://stepimagewm.how2j.cn/9410.png)

出现这个问题肯定是难以避免的，比如数据微服务所在的机房停电了。 
但是这样的提示信息是非常不友好的，客户也看不懂这个是什么。
为了解决这个问题，我们就会引入断路器的概念。

所谓的断路器，就是当被访问的微服务无法使用的时候，当前服务能够感知这个现象，
并且提供一个备用的方案出来。
比如在这个例子里，数据微服务无法使用了，如果有了断路器，
那么视图微服务就能够知道此事，并且展示给用户相关的信息。
而不会报错或者一直卡在那里

![断路器](https://stepimagewm.how2j.cn/9413.png)

pom.xml增加 jar spring-cloud-starter-netflix-hystrix 以支持断路器。

```xml
 <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
    </dependency>   
```

ProductClientFeign启动类

```java
注解由原来的
@FeignClient(value = "PRODUCT-DATA-SERVICE")
修改为
@FeignClient(value = "PRODUCT-DATA-SERVICE",fallback = ProductClientFeignHystrix.class)
```

添加ProductClientFeignHystrix类

ProductClientFeignHystrix 实现了 ProductClientFeign 接口，
并提供了 listProdcuts() 方法。
这个方法就会固定返回包含一条信息的集合

```java
@Component
public class ProductClientFeignHystrix implements ProductClientFeign{
    public List<Product> listProdcuts(){
        List<Product> result = new ArrayList<>();
        result.add(new Product(0,"产品数据微服务不可用",0));
        return result;
    }
 
}
```

修改配置application.yml文件

```yaml
spring:
  application:
    name:  product-view-service-feign
  thymeleaf:
    cache: false
    prefix: classpath:/templates/
    suffix: .html
    encoding: UTF-8
    content-type: text/html
    mode: HTML5      
  zipkin:
    base-url: http://localhost:9411  
 
feign.hystrix.enabled: true
      
management:
  endpoints:
    web:
      exposure:
        include: "*"
      cors:
        allowed-origins: "*"
        allowed-methods: "*"      
```

------

### Spring Cloud ——断路器监控

断路器， 当数据服务不可用的时候， 断路器就会发挥作用。
那么数据服务什么时候可用，什么时候不可用，如何监控这个事情呢。
我们就要用到 断路器监控 来可视化掌控这个情况了。

创建子项目hystrix-dashboard

pom.xml文件导入jar

```xml
 <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
        </dependency>
         
    </dependencies>
```

ProductServiceHystrixDashboardApplication为断路器监控启动类
主要就是@EnableHystrixDashboard 这个

```java
@SpringBootApplication
@EnableHystrixDashboard
public class ProductServiceHystrixDashboardApplication {
    public static void main(String[] args) {
        int port = 8020;
        if(!NetUtil.isUsableLocalPort(port)) {
            System.err.printf("端口%d被占用了，无法启动%n", port );
            System.exit(1);
        }
        new SpringApplicationBuilder(ProductServiceHystrixDashboardApplication.class).properties("server.port=" + port).run(args);
 
    }
 
}
```

修改配置文件application.yml

```yaml
spring:
  application:
    name: hystrix-dashboard
```

修改视图微服务项目，以使得它可以把信息共享给监控中心。
修改ProductViewServiceFeignApplication，
增加 @EnableCircuitBreaker

准备一个不停访问服务的类： AccessViewService。
这样可以不断地访问服务，才便于在监控那里观察现象。

```java
public class AccessViewService {
 
    public static void main(String[] args) {
         
        while(true) {
            ThreadUtil.sleep(1000);
            try {
                String html= HttpUtil.get("http://127.0.0.1:8012/products");
                System.out.println("html length:" + html.length());
            }
            catch(Exception e) {
                System.err.println(e.getMessage());
            }
 
        }
         
    }
}
```

运行相关服务之后

1. 首先挨个运行 EurekaServerApplication, 
ConfigServerApplication, ProductDataServiceApplication， 
ProductViewServiceFeignApplication，
ProductServiceHystrixDashboardApplication

2. 运行视图微服务里的 AccessViewService 
来周期性地访问 http://127.0.0.1:8012/products。
因为只有访问了，监控里才能看到数据。

3. 打开监控地址
   http://localhost:8020/hystrix

   ![先运行，看到效果，再学习](https://stepimagewm.how2j.cn/9420.png)

4. 并在最上面输入http://localhost:8012/actuator/hystrix.stream即可查看监控信息了

监控图解

![监控图解](https://stepimagewm.how2j.cn/9428.png)

如果关闭数据服务

![关闭数据服务](https://stepimagewm.how2j.cn/9429.png)

红色的数据就达到100%

------

### Spring Cloud ——断路器监控

**针对一个微服务**的断路器监控，但是微服务通常会是多个实例组成的一个集群。
倘若集群里的实例比较多，不能挨个去监控这些实例。
有时候，根据集群的需要，会动态增加或者减少实例，监控起来就更麻烦了。
所以为了方便监控集群里的多个实例，
springCloud 提供了一个 turbine 项目，
它的作用是把一个集群里的多个实例汇聚在一个 turbine里，
这个然后再在 断路器监控里查看这个 turbine, 这样就能够在集群层面进行监控

逻辑如图

![需求](https://stepimagewm.how2j.cn/9315.png)

启动相关服务，运行AccessViewService服务（访问才能看到数据）

创建子项目turbine

pom.xml文件导入依赖 jar包

```xml
<dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-turbine</artifactId>
        </dependency>
 
    </dependencies>
```

ProductServiceTurbineApplication为Turbine启动类

```java
@SpringBootApplication
@EnableTurbine
public class ProductServiceTurbineApplication {
    public static void main(String[] args) {
        int port = 8021;
        if(!NetUtil.isUsableLocalPort(port)) {
            System.err.printf("端口%d被占用了，无法启动%n", port );
            System.exit(1);
        }
        new SpringApplicationBuilder(ProductServiceTurbineApplication.class).properties("server.port=" + port).run(args);
 
    }
 
}
```

修改application.yaml文件

```yaml
spring:
  application.name: turbine
turbine:
  aggregator:
    clusterConfig: default  
  appConfig: product-view-service-feign  ### 配置Eureka中的serviceId列表，表明监控哪些服务
  clusterNameExpression: new String("default")
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

并修改AccessViewService类

```java
public class AccessViewService {
  
    public static void main(String[] args) {
          
        while(true) {
            ThreadUtil.sleep(1000);
            access(8012);
            access(8013);
        }
          
    }
  
    public static void access(int port) {
        try {
            String html= HttpUtil.get(String.format("http://127.0.0.1:%d/products",port));
            System.out.printf("%d 地址的视图服务访问成功，返回大小是 %d%n" ,port, html.length());
        }
        catch(Exception e) {
            System.err.printf("%d 地址的视图服务无法访问%n",port);
        }
    }
}
```

然后启动相关服务类，并访问地址进行查看验证

------

### Spring Cloud ——网关Zuul服务

我们现在有两种微服务，分别是数据微服务和视图微服务。
他们有可能放在不同的 ip 地址上，有可能是不同的端口。
为了访问他们，就需要记录这些地址和端口。 而地址和端口都可能会变化，
这就增加了访问者的负担。
所以这个时候，我们就可以用网关来解决这个问题。
如图所示，我们只需要记住网关的地址和端口号就行了。
如果要访问数据服务，访问地址 http://ip:port/api-data/products 即可。
如果要访问视图服务，访问地址 http://ip:port/api-view/products 即可。

逻辑如图

![为何要用网关](https://stepimagewm.how2j.cn/9440.png)

创建子项目Zuul

pom.xml文件导入相关依赖jar包

```xml
<dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-zuul</artifactId>
        </dependency>
    </dependencies>
```

ProductServiceZuulApplication为启动类

启动类，主要是 @EnableZuulProxy

代码比较复制代码

```java
@SpringBootApplication
@EnableZuulProxy
@EnableEurekaClient
@EnableDiscoveryClient
public class ProductServiceZuulApplication {
    public static void main(String[] args) {
        int port = 8040;
        if(!NetUtil.isUsableLocalPort(port)) {
            System.err.printf("端口%d被占用了，无法启动%n", port );
            System.exit(1);
        }
        new SpringApplicationBuilder(ProductServiceZuulApplication.class).properties("server.port=" + port).run(args);
 
    }
}
```

修改application.yml配置文件

```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
spring:
  application:
    name: product-service-zuul
zuul:
  routes:
    api-a:
      path: /api-data/**
      serviceId: PRODUCT-DATA-SERVICE
    api-b:
      path: /api-view/**
      serviceId: PRODUCT-VIEW-SERVICE-FEIGN
```

运行相关服务，然后访问

```http
http://localhost:8040/api-data/products
http://localhost:8040/api-view/products
```