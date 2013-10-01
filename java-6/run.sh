#!/bin/bash

args=(${@// /\\ })

mvn exec:java -q -Dexec.mainClass="com.ksmpartners.rpncalc.App" -Dexec.args="${args[*]}"
