#!/bin/bash

echo "Azure Event Hubs - Premium - Performance Regression Analysis"
TIMESTAMP=`date +%Y-%m-%d_%H-%M-%S`
echo $TIMESTAMP

# AMQP
bin/benchmark --drivers driver-azure-eventhubs/amqp-premium.yaml workloads/1producer-1consumer-50Kb-1Mbps.yaml -o amqp-1producer-1consumer-50Kb-1Mbps.yaml -t Benchmarking,Regression,Latency
bin/benchmark --drivers driver-azure-eventhubs/amqp-premium.yaml workloads/1producer-1consumer-1MBMessage.yaml -o amqp-1producer-1consumer-largemessage -t Benchmarking,Regression,Latency
echo "Completed Execution of AMQP Tests"

#Kafka
bin/benchmark --drivers driver-kafka/kafka-premium.yaml workloads/1producer-1consumer-50Kb-1Mbps.yaml -o kafka-1producer-1consumer-50Kb-1Mbps -t Benchmarking,Regression,Latency
bin/benchmark --drivers driver-kafka/kafka-premium.yaml workloads/1producer-1consumer-1MBMessage.yaml -o kafka-1producer-1consumer-largemessage -t Benchmarking,Regression,Latency
echo "Completed Execution of Benchmarking Tests"