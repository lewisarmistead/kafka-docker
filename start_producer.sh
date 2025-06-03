#!/bin/zsh
docker exec --interactive --tty broker \
kafka-console-producer --bootstrap-server localhost:9092 \
                       --topic example-topic
