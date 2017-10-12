# sqoop_maven_kudu

eg:

```
sqoop import  --connect jdbc:mysql://192.168.1.1/test --username canal -password canal --table xdual5  -m 1  --kudu-master-url 192.168.1.1 --kudu-create-table --kudu-table  xdual5 --kudu-key-cols ID --kudu-partition-cols ID --kudu-partition-buckets 2 --kudu-replica-count 1
```
