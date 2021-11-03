#!/bin/bash

echo "Azure Event Hubs - Premium - Performance Benchmarking"
TIMESTAMP=`date +%Y-%m-%d_%H-%M-%S`
mkdir ./result/$TIMESTAMP



# AMQP
sudo bin/benchmark  --drivers driver-azure-eventhubs/eventhubs-native-premium.yaml workloads/event-hubs/LT-1.1.yaml -o ./result/$TIMESTAMP/amqp-1.1.json
sudo bin/benchmark  --drivers driver-azure-eventhubs/eventhubs-native-premium.yaml workloads/event-hubs/LT-1.4.yaml -o ./result/$TIMESTAMP/amqp-1.4.json
sudo bin/benchmark  --drivers driver-azure-eventhubs/eventhubs-native-premium.yaml workloads/event-hubs/LT-1.8.yaml -o ./result/$TIMESTAMP/amqp-1.8.json
sudo bin/benchmark  --drivers driver-azure-eventhubs/eventhubs-native-premium.yaml workloads/event-hubs/LT-1.16.yaml -o ./result/$TIMESTAMP/amqp-1.16.json
sudo bin/benchmark  --drivers driver-azure-eventhubs/eventhubs-native-premium.yaml workloads/event-hubs/LT-1.32.yaml -o ./result/$TIMESTAMP/amqp-1.32.json
sudo bin/benchmark  --drivers driver-azure-eventhubs/eventhubs-native-premium.yaml workloads/event-hubs/LT-1.64.yaml -o ./result/$TIMESTAMP/amqp-1.64.json
sudo bin/benchmark  --drivers driver-azure-eventhubs/eventhubs-native-premium.yaml workloads/event-hubs/LT-1.100.yaml -o ./result/$TIMESTAMP/amqp-1.100.json

# Kafka
sudo bin/benchmark  --drivers driver-kafka/eventhubs-kafka-premium.yaml  workloads/event-hubs/LT-1.1.yaml -o ./result/$TIMESTAMP/kafka-1.1.json
sudo bin/benchmark  --drivers driver-kafka/eventhubs-kafka-premium.yaml  workloads/event-hubs/LT-1.4.yaml -o ./result/$TIMESTAMP/kafka-1.4.json
sudo bin/benchmark  --drivers driver-kafka/eventhubs-kafka-premium.yaml  workloads/event-hubs/LT-1.8.yaml -o ./result/$TIMESTAMP/kafka-1.8.json
sudo bin/benchmark  --drivers driver-kafka/eventhubs-kafka-premium.yaml  workloads/event-hubs/LT-1.16.yaml -o ./result/$TIMESTAMP/kafka-1.16.json
sudo bin/benchmark  --drivers driver-kafka/eventhubs-kafka-premium.yaml  workloads/event-hubs/LT-1.32.yaml -o ./result/$TIMESTAMP/kafka-1.32.json
sudo bin/benchmark  --drivers driver-kafka/eventhubs-kafka-premium.yaml  workloads/event-hubs/LT-1.64.yaml -o ./result/$TIMESTAMP/kafka-1.64.json
sudo bin/benchmark  --drivers driver-kafka/eventhubs-kafka-premium.yaml  workloads/event-hubs/LT-1.100.yaml -o ./result/$TIMESTAMP/kafka-1.100.json


# AMQP
sudo bin/benchmark  --drivers driver-azure-eventhubs/eventhubs-native-premium.yaml workloads/event-hubs/LT-2.1.yaml -o ./result/$TIMESTAMP/amqp-2.1.json
sudo bin/benchmark  --drivers driver-azure-eventhubs/eventhubs-native-premium.yaml workloads/event-hubs/LT-2.4.yaml -o ./result/$TIMESTAMP/amqp-2.4.json
sudo bin/benchmark  --drivers driver-azure-eventhubs/eventhubs-native-premium.yaml workloads/event-hubs/LT-2.8.yaml -o ./result/$TIMESTAMP/amqp-2.8.json
sudo bin/benchmark  --drivers driver-azure-eventhubs/eventhubs-native-premium.yaml workloads/event-hubs/LT-2.16.yaml -o ./result/$TIMESTAMP/amqp-2.16.json
sudo bin/benchmark  --drivers driver-azure-eventhubs/eventhubs-native-premium.yaml workloads/event-hubs/LT-2.32.yaml -o ./result/$TIMESTAMP/amqp-2.32.json
sudo bin/benchmark  --drivers driver-azure-eventhubs/eventhubs-native-premium.yaml workloads/event-hubs/LT-2.64.yaml -o ./result/$TIMESTAMP/amqp-2.64.json
sudo bin/benchmark  --drivers driver-azure-eventhubs/eventhubs-native-premium.yaml workloads/event-hubs/LT-2.100.yaml -o ./result/$TIMESTAMP/amqp-2.100.json



# Kafka
sudo bin/benchmark  --drivers driver-kafka/eventhubs-kafka-premium.yaml  workloads/event-hubs/LT-2.1.yaml -o ./result/$TIMESTAMP/kafka-2.1.json
sudo bin/benchmark  --drivers driver-kafka/eventhubs-kafka-premium.yaml  workloads/event-hubs/LT-2.4.yaml -o ./result/$TIMESTAMP/kafka-2.4.json
sudo bin/benchmark  --drivers driver-kafka/eventhubs-kafka-premium.yaml  workloads/event-hubs/LT-2.8.yaml -o ./result/$TIMESTAMP/kafka-2.8.json
sudo bin/benchmark  --drivers driver-kafka/eventhubs-kafka-premium.yaml  workloads/event-hubs/LT-2.16.yaml -o ./result/$TIMESTAMP/kafka-2.16.json
sudo bin/benchmark  --drivers driver-kafka/eventhubs-kafka-premium.yaml  workloads/event-hubs/LT-2.32.yaml -o ./result/$TIMESTAMP/kafka-2.32.json
sudo bin/benchmark  --drivers driver-kafka/eventhubs-kafka-premium.yaml  workloads/event-hubs/LT-2.64.yaml -o ./result/$TIMESTAMP/kafka-2.64.json
sudo bin/benchmark  --drivers driver-kafka/eventhubs-kafka-premium.yaml  workloads/event-hubs/LT-2.100.yaml -o ./result/$TIMESTAMP/kafka-2.100.json



# AMQP
sudo bin/benchmark  --drivers driver-azure-eventhubs/eventhubs-native-premium.yaml workloads/event-hubs/LT-3.1.yaml -o ./result/$TIMESTAMP/amqp-3.1.json
sudo bin/benchmark  --drivers driver-azure-eventhubs/eventhubs-native-premium.yaml workloads/event-hubs/LT-3.4.yaml -o ./result/$TIMESTAMP/amqp-3.4.json
sudo bin/benchmark  --drivers driver-azure-eventhubs/eventhubs-native-premium.yaml workloads/event-hubs/LT-3.8.yaml -o ./result/$TIMESTAMP/amqp-3.8.json
sudo bin/benchmark  --drivers driver-azure-eventhubs/eventhubs-native-premium.yaml workloads/event-hubs/LT-3.16.yaml -o ./result/$TIMESTAMP/amqp-3.16.json
#sudo bin/benchmark  --drivers driver-azure-eventhubs/eventhubs-native-premium.yaml workloads/event-hubs/LT-3.32.yaml -o ./result/$TIMESTAMP/amqp-3.32.json
#sudo bin/benchmark  --drivers driver-azure-eventhubs/eventhubs-native-premium.yaml workloads/event-hubs/LT-3.64.yaml -o ./result/$TIMESTAMP/amqp-3.64.json
#sudo bin/benchmark  --drivers driver-azure-eventhubs/eventhubs-native-premium.yaml workloads/event-hubs/LT-3.100.yaml -o ./result/$TIMESTAMP/amqp-3.100.json

# Kafka

sudo bin/benchmark  --drivers driver-kafka/eventhubs-kafka-premium.yaml  workloads/event-hubs/LT-3.1.yaml -o ./result/$TIMESTAMP/kafka-3.1.json
sudo bin/benchmark  --drivers driver-kafka/eventhubs-kafka-premium.yaml  workloads/event-hubs/LT-3.4.yaml -o ./result/$TIMESTAMP/kafka-3.4.json
sudo bin/benchmark  --drivers driver-kafka/eventhubs-kafka-premium.yaml  workloads/event-hubs/LT-3.8.yaml -o ./result/$TIMESTAMP/kafka-3.8.json
sudo bin/benchmark  --drivers driver-kafka/eventhubs-kafka-premium.yaml  workloads/event-hubs/LT-3.16.yaml -o ./result/$TIMESTAMP/kafka-3.16.json
sudo bin/benchmark  --drivers driver-kafka/eventhubs-kafka-premium.yaml  workloads/event-hubs/LT-3.32.yaml -o ./result/$TIMESTAMP/kafka-3.32.json
#sudo bin/benchmark  --drivers driver-kafka/eventhubs-kafka-premium.yaml  workloads/event-hubs/LT-3.64.yaml -o ./result/$TIMESTAMP/kafka-3.64.json
#sudo bin/benchmark  --drivers driver-kafka/eventhubs-kafka-premium.yaml  workloads/event-hubs/LT-3.100.yaml -o ./result/$TIMESTAMP/kafka-3.100.json








