#!/bin/bash

echo "Remove all containers."
docker-compose down -v
sleep 1

rm -f docker-compose.yml
echo "Done."