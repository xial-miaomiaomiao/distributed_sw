#!/bin/bash

# 构建项目
mvn clean package

# 启动应用
java -jar target/sw_3-1.0-SNAPSHOT.jar