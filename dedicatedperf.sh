#!/bin/bash

echo "Azure Event Hubs - Premium - Performance Benchmarking"
TIMESTAMP=`date +%Y-%m-%d_%H-%M-%S`
echo $TIMESTAMP

# AMQP
bin/benchmark --drivers driver-azure-eventhubs/amqp-dedicated-v2.yaml workloads/1producer-1consumer-1MBMessage.yaml -o amqpdedicatedv2-1producer-1consumer-largemessage
bin/benchmark --drivers driver-azure-eventhubs/amqp-dedicated-v2.yaml workloads/4producer-1consumer-1Mbps.yaml -o amqpdedicatedv2-4producer-1consumer-1MBPS
bin/benchmark --drivers driver-azure-eventhubs/amqp-dedicated-v2.yaml workloads/1producer-4consumer-1Mbps.yaml -o amqpdedicatedv2-1producer-4consumer-1MBPS
bin/benchmark --drivers driver-azure-eventhubs/amqp-dedicated-v2.yaml workloads/4producer-4consumer-1Mbps.yaml -o amqpdedicatedv2-4producer-4consumer-1MBPS
bin/benchmark --drivers driver-azure-eventhubs/amqp-dedicated-v2.yaml workloads/1producer-1consumer-1Mbps-400KB.yaml -o amqpdedicatedv2-1producer-1consumer-400Kb
bin/benchmark --drivers driver-azure-eventhubs/amqp-dedicated-v2.yaml workloads/1producer-1consumer-1Mbps-100KB.yaml -o amqpdedicatedv2-1producer-1consumer-100Kb
bin/benchmark --drivers driver-azure-eventhubs/amqp-dedicated-v2.yaml workloads/1producer-1consumer-1Mbps.yaml -o amqpdedicatedv2-1producer-1consumer-1MBPS
echo "Completed Execution of AMQP Tests"

#Kafka
bin/benchmark --drivers driver-kafka/kafka-dedicated-v2.yaml workloads/1producer-1consumer-1MBMessage.yaml -o kafkadedicatedv2-1producer-1consumer-largemessage
bin/benchmark --drivers driver-kafka/kafka-dedicated-v2.yaml workloads/1producer-1consumer-1Mbps.yaml -o kafkadedicatedv2-1producer-1consumer-1MBPS
bin/benchmark --drivers driver-kafka/kafka-dedicated-v2.yaml workloads/4producer-1consumer-1Mbps.yaml -o kafkadedicatedv2-4producer-1consumer-1MBPS
bin/benchmark --drivers driver-kafka/kafka-dedicated-v2.yaml workloads/1producer-4consumer-1Mbps.yaml -o kafkadedicatedv2-1producer-4consumer-1MBPS
bin/benchmark --drivers driver-kafka/kafka-dedicated-v2.yaml workloads/4producer-4consumer-1Mbps.yaml -o kafkadedicatedv2-4producer-4consumer-1MBPS
bin/benchmark --drivers driver-kafka/kafka-dedicated-v2.yaml workloads/1producer-1consumer-1Mbps-400KB.yaml -o kafkadedicatedv2-1producer-1consumer-400Kb
bin/benchmark --drivers driver-kafka/kafka-dedicated-v2.yaml workloads/1producer-1consumer-1Mbps-100KB.yaml -o kafkadedicatedv2-1producer-1consumer-100Kb

echo "Completed Execution of Benchmarking Tests"