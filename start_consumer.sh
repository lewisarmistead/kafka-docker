#!/bin/zsh
docker exec --interactive --tty broker \
kafka-console-consumer --bootstrap-server localhost:9092 \
                       --topic example-topic-transformed \
                       --from-beginning
