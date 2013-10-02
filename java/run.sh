#!/bin/bash

args=(${@// /\\ })

mvn clean install && \
  mvn exec:java -q -Dexec.mainClass="com.ksmpartners.rpncalc.App" -Dexec.args="${args[*]}"
