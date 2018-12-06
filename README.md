# Spring-Boot-JPA杂技用法


前段时间参与了一个使用jhipster写的一个商业项目，在coding的时候遇到需要使用JPA复杂查询的问题（当然可以写原生SQL语句，但这样还要JPA干嘛呢），发现网上相关的信息和资源十分稀少，甚至翻墙都找不到，于是在项目结束后构建一个简单的Springboot应用来做一些总结。

## 简介

此应用是类似qq空间说说的系统，有user和message两个数据库实体，用户注册后可以不断发布说说，user存用户信息，message储存说说。当然这是个Demo应用，并不是为了实现而做，而是为了尽可能多的记录一些Jpa的用法。

## 结构

## 内容

我们将使用到：

1. JPA 的接口Repository简单查询，筛选，统计
2. JPA 的接口Repository.FindALL(Specification)查询
3. EntityManager查询
4. 原生sql查询

### 其他
1. Swagger 文档
2. junit 单元测试
3. p6spy-SQL日志监视
4. H2内存数据库 单元测试数据库隔离
5. springboot配置日志等级
6. org.hibernate-jpamodelgen生成JPA元模型实现类型安全