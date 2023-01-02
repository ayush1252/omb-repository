#!/bin/bash

echo "Azure Event Hubs - Premium - Performance Benchmarking"
TIMESTAMP=`date +%Y-%m-%d_%H-%M-%S`
echo $TIMESTAMP

# AMQP
bin/benchmark --drivers driver-azure-eventhubs/amqp-premium.yaml workloads/1producer-1consumer-1Mbps-20mins.yaml -o amqp-1producer-1consumer-1MBPS-20min
bin/benchmark --drivers driver-azure-eventhubs/amqp-premium.yaml workloads/1producer-1consumer-1MBMessage-20mins.yaml -o amqp-1producer-1consumer-largemessage-20min
bin/benchmark --drivers driver-azure-eventhubs/amqp-premium.yaml workloads/4producer-1consumer-1Mbps-20mins.yaml -o amqp-4producer-1consumer-1MBPS-20min

#Kafka
bin/benchmark --drivers driver-kafka/kafka-premium.yaml workloads/1producer-1consumer-1Mbps-20mins.yaml -o kafka-1producer-1consumer-1MBPS-20min
bin/benchmark --drivers driver-kafka/kafka-premium.yaml workloads/1producer-1consumer-1MBMessage-20mins.yaml -o kafka-1producer-1consumer-largemessage-20min
bin/benchmark --drivers driver-kafka/kafka-premium.yaml workloads/4producer-1consumer-1Mbps-20mins.yaml -o kafka-4producer-1consumer-1MBPS-20min

echo "Completed Execution of Benchmarking Tests"