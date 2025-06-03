#!/bin/zsh
cd kafka-streams-transform
mvn clean package &&  mvn exec:java -Dexec.mainClass="com.example.BasicTransformApp"