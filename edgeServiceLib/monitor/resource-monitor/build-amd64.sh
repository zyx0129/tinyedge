module_name="resource-monitor"
architecture="amd64"
docker build -t $module_name .
docker tag $module_name 47.96.155.111:5000/tinyedge/$module_name:$architecture
docker tag $module_name 47.96.155.111:5000/tinyedge/$module_name
docker push 47.96.155.111:5000/tinyedge/$module_name:$architecture
rm ~/.docker/manifests/47.96.155.111-5000_tinyedge_$module_name-latest/*
docker manifest create --insecure --amend 47.96.155.111:5000/tinyedge/$module_name 47.96.155.111:5000/tinyedge/$module_name:amd64 47.96.155.111:5000/tinyedge/$module_name:arm32
docker manifest push --insecure 47.96.155.111:5000/tinyedge/$module_name