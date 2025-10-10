#!/bin/bash

echo "=== Checking existing AdTasks for Round 33 ==="
curl -X GET "http://localhost:8080/api/ad-tasks/rounds/33" \
  -H "Content-Type: application/json"

echo -e "\n\n=== Checking Round 33 info ==="
curl -X GET "http://localhost:8080/api/rounds/33" \
  -H "Content-Type: application/json"