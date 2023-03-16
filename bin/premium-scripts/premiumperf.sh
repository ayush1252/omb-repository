#!/bin/bash

echo "Azure Event Hubs - Premium - Performance Benchmarking"
TIMESTAMP=`date +%Y-%m-%d_%H-%M-%S`
echo $TIMESTAMP

# AMQP
bin/benchmark --drivers driver-azure-eventhubs/amqp-premium.yaml workloads/1producer-1consumer-4KB.yaml.yaml -o amqp-1producer-1consumer-4KB -t Benchmarking,Latency
bin/benchmark --drivers driver-azure-eventhubs/amqp-premium.yaml workloads/1producer-1consumer-1Mbps-100KB.yaml -o amqp-1producer-1consumer-100Kb -t Benchmarking,Latency
bin/benchmark --drivers driver-azure-eventhubs/amqp-premium.yaml workloads/1producer-1consumer-1Mbps-400KB.yaml -o amqp-1producer-1consumer-400Kb -t Benchmarking,Latency
echo "Completed Execution of AMQP Tests"
#Kafka
bin/benchmark --drivers driver-kafka/kafka-premium.yaml workloads/1producer-1consumer-4KB.yaml -o kafka-1producer-1consumer-4KB -t Benchmarking,Latency
bin/benchmark --drivers driver-kafka/kafka-premium.yaml workloads/1producer-1consumer-1Mbps-100KB.yaml -o kafka-1producer-1consumer-100Kb -t Benchmarking,Latency
bin/benchmark --drivers driver-kafka/kafka-premium.yaml workloads/1producer-1consumer-1Mbps-400KB.yaml -o kafka-1producer-1consumer-400Kb -t Benchmarking,Latency
echo "Completed Execution of Benchmarking Tests"