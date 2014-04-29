bison
=====

Bison 是一个JAVA 程间的通信框架，基于apache mina 实现，对mina进行了byteBuffer 缓冲区重用以及半包出处时减少拷贝。

客户端(bison-client) 功能点

1 业务分组。

2 支持分组内负载均衡。

3 支持横向扩展。

服务端(bison)

1 支持热部署

2 支持流控，防止服务端出现拥堵时，客户端请求可以快速失败。

3 资源统一管理。数据库连接池，工作线程,等。

4 支持白名单，黑名单。

计划

1 支持netty做底层通信。

使用说明

1 download bison和bison client到本地，再用maven导入到eclipse的工作空间。

2 依赖，pom文件里面依赖的我们自己优化过的mina的版本，目前这个还没有放出来，所以你需要修改为社区的稳定的版本，不然会编译不通过。

3 部署 通过maven assembly打成zip包后，上传到服务器，解压zip包，会包含如下目录：

 bin (启动和停止脚步)
 lib(bison依赖的第三方库)
 service(业务相关的jar包放这里,该目录下还可以有classes目录，如果需要更新class，就放到这个目录下即可)
 conf(bison的配置文件目录)
 logs(bison的日志目录)

 注意：我把进程的pid也指定了写在bison的根目录下，所有如果你不想修改启动脚本，则需要再该目录下创建一个pid目录。用来存储bison的进程id。

4 按上面部署完后，在bison目录下，执行./bin/start-bison.sh

5 停止服务，在bison目录下，执行./bin/stop-bison.sh.

6 在Windows环境下测试，可以通过 bin/start-bison.cmd脚本来启动。

7 测试demo 在bison-client 项目里有相关测试的demo。分为远程接口调用和远程bean调用。在使用前，你需要把对应的实现类打成jar包，上传到bison的service目录下。不然会报找不到class的错误，还有需要注意的是，接口和实现需要在相同的包路径下，最好是接口定义和实现在一个jar里。


bison 简单易用的RPC框架，对外就依赖mina，没有太多的配置，很容部署，如果你有什么问题，请@新浪微博@深海之倚天剑(http://weibo.com/jamvp)给我发消息。
