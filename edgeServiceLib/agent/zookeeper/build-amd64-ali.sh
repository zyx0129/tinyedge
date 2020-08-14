url="registry.cn-hangzhou.aliyuncs.com"
old_name="wurstmeister/zookeeper"
namespace="tinyedge"
module_name="zookeeper"
architecture="amd64"
version="v1.0"
#docker build -t ${module_name} .
docker tag ${old_name} ${url}/${namespace}/${module_name}:${architecture}-${version}
docker tag ${old_name} ${url}/${namespace}/${module_name}
docker tag ${old_name} ${url}/${namespace}/${module_name}:${version}
docker push ${url}/${namespace}/${module_name}:$architecture-${version}
rm ~/.docker/manifests/${url}_${namespace}_${module_name}-latest/*
rm ~/.docker/manifests/${url}_${namespace}_${module_name}-${version}/*
docker manifest create --amend ${url}/${namespace}/${module_name} ${url}/${namespace}/${module_name}:amd64-${version} ${url}/${namespace}/${module_name}:armv7l-${version}
docker manifest push ${url}/${namespace}/${module_name}
docker manifest create --amend ${url}/${namespace}/${module_name}:${version} ${url}/${namespace}/${module_name}:amd64-${version} ${url}/${namespace}/${module_name}:armv7l-${version}
docker manifest push ${url}/${namespace}/${module_name}:${version}
