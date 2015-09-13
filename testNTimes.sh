#!/bin/bash
mvn clean
for (( i = 1; i < $1 + 1 ; i++ ))
do
  echo "Round $i"
  mvn test -o -Dtest=$2
  exitcode=$?
  if [ $exitcode -ne 0 ]
  then
    echo "Error at round $i"
    exit
  fi
done
