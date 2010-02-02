#!/bin/bash
#
# Generate a valid tsv file from one of the queries in this directory
# ./generatetsv.sh arrival
#
MYSQLPWD=sasa
MYSQLUSER=root
SCRIPT=$1
ROWCOUNT=${2:-50}
BASENAME=${SCRIPT%%.*}

if [ -z "${SCRIPT}" ]; then
    echo "Pass scripts/dataset/<table-name>.sql name as an argument"
    exit 1
fi

mysql -u${MYSQLUSER} -p${MYSQLPWD} -B < scripts/setup/setup.sql

#echo "Using script $1. Creating tsv with $ROWCOUNT rows."

#rm tempdata
echo "use colony;" > tempscript
cat $1 >> tempscript
echo " ORDER BY DATE DESC" >> tempscript
echo " LIMIT $ROWCOUNT" >> tempscript
echo " ;" >> tempscript
mysql -u${MYSQLUSER} -p${MYSQLPWD} -B < tempscript | sed s/NULL//g
if [ $? -ne 0 ]; then
    echo "ERROR trying to setup dump table using script $1"
    exit 1
fi

#java FixTabs < tempdata >> temptsv

