# 瑞吉外卖实现
## 避免前端（JavaScript）处理大数（如 Long、BigInteger）时发生精度丢失问题，所以引入了自定义 Jackson 配置。
先看代码：
```java
 /*
    * 根据id修改员工信息*/
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());

        Long empId = (Long)request.getSession().getAttribute("employee");
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(empId);
        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }
```
这里由于要修改的员工信息的id是通过mp雪花算法得到的超长数字，js前端在访问这个数据的时候会出现精度损失，导致后端拿不到这个id，因此无法更新数据
### 1. jackson 是什么？
Jackson 是一个功能强大的 Java 类库，主要用于在 Java 对象 和 JSON 数据之间做转换。
它可以：

把 Java 对象 转成 JSON 字符串（序列化）

把 JSON 字符串 解析成 Java 对象（反序列化）

你可以把 Jackson 理解为 Java 世界里的 "JSON翻译器"。

官网地址：https://github.com/FasterXML/jackson

在 Java 里常用的 JSON 处理库有：

Jackson （最流行）

Gson （Google出的，也挺常见）

Fastjson （阿里出的，国内有些公司用）

其中 Jackson 在 Spring Boot 里默认就是集成的（不用特地引）。
这里我们用json来处理
### 2. jackson 和 json 是什么关系？
JSON（JavaScript Object Notation） 是一种数据交换格式，本身跟 Jackson 没有直接关系。

Jackson 是处理 JSON 的工具，是帮你在 Java 中读写 JSON 的 实现库。

换句话说，JSON 是标准，Jackson 是工具。
就像：“水（JSON）是资源，桶（Jackson）是工具”，你用 Jackson 来搬运、转换 JSON 数据。
### 为什么要特别处理 Long / BigInteger？
这个非常关键！

原因是 JavaScript 的 number 类型（双精度浮点数）在 2^53（大约 16位整数）之后就会失真。
在前端（特别是Vue、React）里，如果后端直接返回数字格式的 Long 或 BigInteger，前端 JSON.parse() 后就精度丢了！

所以你要在后端 把这些大整数转成字符串输出，前端才能安全处理，比如：
```json
{
  "orderId": "9223372036854775807"
}

```
前端拿到字符串后，自己解析或展示，不会丢精度！
因此我们要创建自定义模块来注册，序列化器，反序列化器

### 自定义Jackson ObjectMapper 
```java
SimpleModule simpleModule = new SimpleModule()
```
#### 序列化器
这个是反序列化器(json->java对象):
```java
.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)))
.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)))
```
这里针对 Java 8 时间类型（LocalDateTime、LocalDate、LocalTime）指定了解析格式。

例如，遇到 "2025-04-28 12:00:00" 这样的字符串时，能正确反序列化成 LocalDateTime。

#### 反序列化器
接着这里用反序列化器(java对象->json):
```java
.addSerializer(BigInteger.class, ToStringSerializer.instance)
.addSerializer(Long.class, ToStringSerializer.instance)
.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)))
.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));

```
将 BigInteger 和 Long 类型序列化为字符串（防止前端 JavaScript 解析大整数丢失精度问题）。

将 LocalDateTime、LocalDate、LocalTime 使用指定格式序列化成字符串。

### jackson整体关系类图
```mermaid
classDiagram
    class JSON {
        <<interface>>
        +数据格式标准
    }
    
    class ObjectMapper {
        +writeValueAsString(Object) String
        +readValue(String, Class) T
    }

    class JacksonObjectMapper {
        +DEFAULT_DATE_FORMAT : String
        +DEFAULT_DATE_TIME_FORMAT : String
        +DEFAULT_TIME_FORMAT : String
        +JacksonObjectMapper()
    }

    JSON <.. ObjectMapper : 处理
    ObjectMapper <|-- JacksonObjectMapper : 继承
```
### jacksonObjectMapper结构图 
```mermaid
classDiagram
    class JacksonObjectMapper {
        +DEFAULT_DATE_FORMAT : String
        +DEFAULT_DATE_TIME_FORMAT : String
        +DEFAULT_TIME_FORMAT : String
        +JacksonObjectMapper()
        +configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
        +registerModule(SimpleModule)
    }

    class SimpleModule {
        +addDeserializer(Type, Deserializer)
        +addSerializer(Type, Serializer)
    }

    class LocalDateTimeDeserializer
    class LocalDateDeserializer
    class LocalTimeDeserializer
    class LocalDateTimeSerializer
    class LocalDateSerializer
    class LocalTimeSerializer
    class ToStringSerializer

    JacksonObjectMapper --> SimpleModule : 注册模块
    SimpleModule --> LocalDateTimeDeserializer : 添加
    SimpleModule --> LocalDateDeserializer : 添加
    SimpleModule --> LocalTimeDeserializer : 添加
    SimpleModule --> LocalDateTimeSerializer : 添加
    SimpleModule --> LocalDateSerializer : 添加
    SimpleModule --> LocalTimeSerializer : 添加
    SimpleModule --> ToStringSerializer : 添加
```
### 扩展mvc架构的消息转换器
前面知识配置了jackson的信息，但是还没有完成实现，由于后端发给前端的信息的json格式的，而包装发送json数据是mvc设置的，所以我们还需要在mvc配置类中加入扩展mvc架构信息转换器
具体代码如下：
```java
/*
    * 扩展mvc框架的消息转换器
    * */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        //创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        //设置对象转换器，底层使用jackson将java转成json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //将上面的消息转换器对象追加到mvc框架的转换器集合中
        converters.add(0,messageConverter);
    }
```
但是这里由于我们扩展了 SpringMVC 配置，导致 Spring Boot 自动配置失效了。 我们继承了一个 MVC 配置类，打破了默认的静态资源映射规则，在 Spring Boot 中（比如用 spring-boot-starter-web）：
默认情况下，Spring Boot 自动帮你配置好静态资源访问路径，比如：
/static/

/public/

/resources/

/META-INF/resources/
只要把 HTML、CSS、JS 放在 static 里，可以直接通过 URL 访问，无需自己写 addResourceHandlers()。但是！！ 一旦手动继承了 SpringMVC 配置，即使你只是重写 extendMessageConverters()，Spring Boot会认为你要接管整个SpringMVC配置！
于是，Spring Boot默认的静态资源映射失效了。
重写静态资源映射就可以了：
```java
/*
    * 设置静态资源映射*/
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始进行静态资源映射...");
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/static/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/static/front/");
    }
```

## 公共字段自动填充、
前面我们完成后台系统员工管理功能开发，在新增员工时需要设置创建时间、创建人、修改时间、修改人等字段，在编辑员工时需要设置修改时间和修改人等字段，这些属于公共字段
能不能直接对这些公共字段在某一个地方统一处理，来简化开发？
可以用Mybatis Plus提供的公共字段自动填充功能（实际上也可以用Spring自带的AOP功能实现）

### 1、在实体类的属性上加入@TableField注解，指定自动填充的策略
```java
@TableField(fill = FieldFill.INSERT)//插入时填充字段
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)//插入和更新时填充字段
    private Long updateUser;
```
### 2、按照框架要求编写元数据对象处理器，在此类中统一为公共字段赋值，此类需要实现MetaObjectHandler接口
```java
/*
 * 自定义原数组对象处理器*/
@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
    //这里metaObject实际上是元数据
    /*
     * 插入操作，自动填充*/
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("公共字段自动填充[insert]...");
        log.info(metaObject.toString());
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime",LocalDateTime.now());
        metaObject.setValue("createUser",new Long(1));//由于我们拿不到request请求中的session，无法获得当前用户的id，这里先用固定属性
        metaObject.setValue("updateUser",new Long(1));
    }

    /*
     * 更新操作，自动填充*/
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共字段自动填充[update]...");
        log.info(metaObject.toString());
        metaObject.setValue("updateTime",LocalDateTime.now());
        metaObject.setValue("updateUser",new Long(1));
    }
}
```
这样就可以不用每次set公共属性，之前更新员工信息和新增员工信息中对应的代码也可以注释掉了

### ThreadLocal
由于我们拿不到request请求中的session，无法获得当前用户的id，所以不够完全，这里需要用ThreadLocal线程技术去实现
客户端每次发送http请求，对应的服务端都会分配一个新的线程来处理，在处理过程中设计下面类中的方法都属于同一个线程：
1、LoginCheckFilter的doFilter方法
2、EmployeeController的update方法
3、MyMetaObjectHandle的updateFill方法
可以在上面的三个方法中分别加入下面代码（获取当前线程id）：
```java
long id = Thread.currentThread().getId();
log.info("线程id：（）",id);
```
运行可以看到是同个线程id
![image](https://github.com/user-attachments/assets/3005ec8a-6813-4287-988f-e625963e2aa3)

#### 什么是ThreadLocal？
ThreadLocal并不是一个Thread，而是Thread的局部变量。当使用ThreadLocal维护变量时，ThreadLocal为每个使用该变量的线程提供独立的变量副本
所以每一个线程都可以独立地改变自己的副本，而不会影响其他线程对应的副本
ThreadLocal为每一个线程提供单独一份存储空间，具有线程隔离的效果，只有在线程内才能获取对应的值，线程外则不能访问

ThreadLocal常用方法：
public void set(T value) 设置当前线程的线程局部变量的值
public T get() 返回当前线程所对应的线程局部变量的值

那么我们就可以在LoginCheckFilter中的doFilter方法中获取当前登录用户的id，并调用set来设置当前线程局部变量的值（用户id），
然后在MyMetaObjectHandler的updateFill方法中调用get来获取线程对应局部变量值（用户id），也算是曲线救国了hhh

实现步骤：
1、编写BaseContext工具类，基于ThreadLocal封装的工具类
```java
/*
* 基于ThreadLocal封装工具类，用户保存和获取当前登录用户id*/
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
```
2、在LoginCheckFilter的doFilter方法中调用BaseContext来设置当前登录用户的id
```java
if(request.getSession().getAttribute("employee") != null){
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("employee"));
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request,response);
            return;
        }
```
3、在MyMetaObjectHandler的方法中调用BaseContext获取登录用户id
```java
public class MyMetaObjectHandler implements MetaObjectHandler {
    //这里metaObject实际上是元数据
    /*
    * 插入操作，自动填充*/
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("公共字段自动填充[insert]...");
        log.info(metaObject.toString());
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime",LocalDateTime.now());
        metaObject.setValue("createUser",BaseContext.getCurrentId());
        metaObject.setValue("updateUser",BaseContext.getCurrentId());
    }

    /*
    * 更新操作，自动填充*/
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共字段自动填充[update]...");
        log.info(metaObject.toString());

        long id = Thread.currentThread().getId();
        log.info("线程id为：{}",id);

        metaObject.setValue("updateTime",LocalDateTime.now());
        metaObject.setValue("updateUser",BaseContext.getCurrentId());
    }
}
```
