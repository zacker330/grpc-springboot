## Demo of gRpc integrate with Springboot

### run gRpc server
```bash
./gradlew :grpc-springboot-server:bootRun
```


### prometheus export endpoint
```bash
curl 192.168.0.104:8091/metrics
```

### about dev
for app's metrics, you can setup a prometheus+grafana stack 
with this repo: https://github.com/vegasbrianc/prometheus 