#!/usr/bin/env bash
set -e

CONTAINER_NAME=${KAFKA_CONTAINER:-roadrescue-kafka}
KAFKA_BOOTSTRAP_SERVER=${KAFKA_BOOTSTRAP_SERVER:-localhost:9092}

# Determine if we should run inside Docker container or locally
if command -v kafka-topics >/dev/null 2>&1; then
  KAFKA_CMD="kafka-topics"
elif docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
  echo "Using Docker container '${CONTAINER_NAME}' to execute kafka-topics..."
  KAFKA_CMD="docker exec ${CONTAINER_NAME} kafka-topics"
else
  echo "Error: Neither 'kafka-topics' CLI nor running Docker container '${CONTAINER_NAME}' was found."
  exit 1
fi

echo "Creating RoadRescue Kafka topics at ${KAFKA_BOOTSTRAP_SERVER}..."

topics=(
  "booking-created-topic"
  "provider-search-started-topic"
  "provider-assigned-topic"
  "provider-accepted-topic"
  "provider-rejected-topic"
  "provider-en-route-topic"
  "provider-arrived-topic"
  "service-started-topic"
  "service-completed-topic"
  "invoice-created-topic"
  "payment-initiated-topic"
  "payment-succeeded-topic"
  "payment-failed-topic"
  "booking-cancelled-topic"
  "provider-availability-changed-topic"
  "provider-location-updated-topic"
)

for topic in "${topics[@]}"; do
  echo "Creating topic: ${topic}"
  ${KAFKA_CMD} --bootstrap-server "${KAFKA_BOOTSTRAP_SERVER}" \
    --create --if-not-exists \
    --topic "${topic}" \
    --partitions 3 \
    --replication-factor 1 || true
done

echo "All RoadRescue Kafka topics created successfully."
