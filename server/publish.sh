#!/bin/sh

echo "------------ PUBLISH $1 ------------"

curl --header 'Content-Type:application/octet-stream' --data-binary @$1 http://localhost:8080/api/-/publish
echo "\n"
