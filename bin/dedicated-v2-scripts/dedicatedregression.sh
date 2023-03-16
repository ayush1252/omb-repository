#!/bin/bash

echo "Azure Event Hubs - Premium - Performance Regression Analysis"
TIMESTAMP=`date +%Y-%m-%d_%H-%M-%S`
echo $TIMESTAMP

# AMQP
bin/benchmark --drivers driver-azure-eventhubs/amqp-dedicated-v2.yaml workloads/1producer-1consumer-1MBMessage.yaml -o amqpdedicatedv2-1producer-1consumer-largemessage -t Benchmarking,Regression,Latency
bin/benchmark --drivers driver-azure-eventhubs/amqp-dedicated-v2.yaml workloads/1producer-1consumer-50Kb-1Mbps.yaml -o amqpdedicatedv2-1producer-1consumer-1MBPS -t Benchmarking,Regression,Latency
echo "Completed Execution of AMQP Tests"

#Kafka
bin/benchmark --drivers driver-kafka/kafka-dedicated-v2.yaml workloads/1producer-1consumer-1MBMessage.yaml -o kafkadedicatedv2-1producer-1consumer-largemessage -t Benchmarking,Regression,Latency
bin/benchmark --drivers driver-kafka/kafka-dedicated-v2.yaml workloads/1producer-1consumer-50Kb-1Mbps.yaml -o kafkadedicatedv2-1producer-1consumer-1MBPS -t Benchmarking,Regression,Latency
echo "Completed Execution of Benchmarking Tests"