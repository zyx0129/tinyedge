url="registry.cn-hangzhou.aliyuncs.com"
namespace="tinyedge"
module_name="object-recognition"
architecture="amd64"
version="v1.0"
docker build -t $module_name .
docker tag $module_name $url/$namespace/$module_name:$architecture-$version
docker tag $module_name $url/$namespace/$module_name
docker tag $module_name $url/$namespace/$module_name:$version
docker push $url/$namespace/$module_name:$architecture-$version
rm ~/.docker/manifests/$url_$namespace_$module_name-latest/*
docker manifest create --amend $url/$namespace/$module_name $url/$namespace/$module_name:amd64-$version $url/$namespace/$module_name:arm32-$version
docker manifest push $url/$namespace/$module_name
docker manifest create --amend $url/$namespace/$module_name:$version $url/$namespace/$module_name:amd64-$version $url/$namespace/$module_name:arm32-$version
docker manifest push $url/$namespace/$module_name:$version
