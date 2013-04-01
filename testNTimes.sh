#!/bin/bash
for (( i = 1; i < $1 ; i++ ))
do
  echo "Attempt $i"
  mvn test -o -Dtest=$2
  exitcode=$?
  if [ $exitcode -ne 0 ]
  then
    echo "Error at attempt $i"
    exit
  fi
done
