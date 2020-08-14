module_name="mqtt-broker"
docker manifest create --insecure 47.96.155.111:5000/tinyedge/$module_name 47.96.155.111:5000/tinyedge/$module_name:amd64 47.96.155.111:5000/tinyedge/$module_name:arm32
docker manifest push --insecure 47.96.155.111:5000/tinyedge/$module_name