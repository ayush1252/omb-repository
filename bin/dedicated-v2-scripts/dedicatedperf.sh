#!/bin/bash

echo "Azure Event Hubs - Premium - Performance Benchmarking"
TIMESTAMP=`date +%Y-%m-%d_%H-%M-%S`
echo $TIMESTAMP

# AMQP
bin/benchmark --drivers driver-azure-eventhubs/amqp-dedicated-v2.yaml workloads/1producer-1consumer-4KB.yaml -o amqpdedicatedv2-1producer-1consumer-4KB -t Benchmarking,Latency
bin/benchmark --drivers driver-azure-eventhubs/amqp-dedicated-v2.yaml workloads/1producer-1consumer-1Mbps-100KB.yaml -o amqpdedicatedv2-1producer-1consumer-100Kb -t Benchmarking,Latency
bin/benchmark --drivers driver-azure-eventhubs/amqp-dedicated-v2.yaml workloads/1producer-1consumer-1Mbps-400KB.yaml -o amqpdedicatedv2-1producer-1consumer-400Kb -t Benchmarking,Latency
bin/benchmark --drivers driver-azure-eventhubs/amqp-batch-dedicated-v2.yaml workloads/1producer-1consumer-50Kb-1Mbps.yaml -o amqpdedicatedv2-1producer-1consumer-1MBPS-batch -t Benchmarking,Latency,Batch
echo "Completed Execution of AMQP Tests"

#Kafka
bin/benchmark --drivers driver-kafka/kafka-dedicated-v2.yaml workloads/1producer-1consumer-4KB.yaml -o kafkadedicatedv2-1producer-1consumer-4KB -t Benchmarking,Latency
bin/benchmark --drivers driver-kafka/kafka-dedicated-v2.yaml workloads/1producer-1consumer-1Mbps-100KB.yaml -o kafkadedicatedv2-1producer-1consumer-100Kb -t Benchmarking,Latency
bin/benchmark --drivers driver-kafka/kafka-dedicated-v2.yaml workloads/1producer-1consumer-1Mbps-400KB.yaml -o kafkadedicatedv2-1producer-1consumer-400Kb -t Benchmarking,Latency
bin/benchmark --drivers driver-kafka/kafka-batch-dedicated-v2.yaml workloads/1producer-1consumer-50Kb-1Mbps.yaml -o kafkadedicatedv2-1producer-1consumer-1MBPS-batch -t Benchmarking,Latency,Batch

echo "Completed Execution of Benchmarking Tests"