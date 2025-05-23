# 公交管理系统

## 项目简介
公交管理系统是一个基于数据库的管理系统，旨在帮助公交公司管理车队、司机、车辆和违章信息。系统通过规范化设计，避免数据冗余和异常问题，同时提供高效的数据查询和统计功能。

## 功能需求

### 数据库功能需求
- **规范化设计**：应用规范化理论，避免插入异常、删除异常和数据冗余。
- **完整性规则**：设定实体完整性（主码）、参照完整性（外码）和用户自定义完整性（如性别限制）。
- **索引优化**：使用索引加快查询速度。
- **视图、函数和存储过程**：简化系统设计，提供复杂查询和操作的封装。

### 前台程序功能需求
- **司机信息管理**：录入司机基本信息（工号、姓名、性别等）。
- **车辆信息管理**：录入汽车基本信息（车牌号、座数等）。
- **违章信息管理**：录入司机的违章信息。
- **查询功能**：
查询某个车队下的所有司机基本信息。
查询某名司机在某个时间段的违章详细信息。
查询某个车队在某个时间段的违章统计信息。

## ER图设计
![ER图](ER图.jpg)


## 程序开发环境及应用环境
- **操作系统**：Windows
- **编程语言**：SQL + Java
- **数据库**：PostgreSQL
- **开发工具**：IntelliJ IDEA

## 应用程序设计中遇到的问题及解决方法

1. **循环引用问题**
- 问题：Driver 表和 Fleet 表之间的循环引用导致数据插入困难和更新冲突。
- 解决：移除 Fleet 表中引用 Driver 表的字段。
2. **冗余字段问题**
- 问题：Driver 表中存在冗余字段，导致数据冗余。
- 解决：优化表结构，移除冗余字段。
3. **索引优化不足**
- 问题：外键字段和频繁查询字段缺乏索引，导致查询效率低下。
- 解决：为外键和频繁查询字段添加索引。
4. **代码冗余**
- 问题：司机、路队长和车队队长的类中存在重复代码。
- 解决：引入抽象基类或接口，提取公共方法，减少代码冗余。

## 总结
通过公交安全管理系统项目，我深入理解数据库设计的基本原则，包括规范化理论、关系模型和完整性约束，并掌握了使用 SQL 语言进行数据库创建、表结构定义、索引建立、视图和函数开发等技能。在解决实际问题的过程中，如循环引用和代码冗余等问题，虽然尚未完全解决，但显著提升了我的问题分析和解决能力。
