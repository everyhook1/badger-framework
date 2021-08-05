cd badger-example/k8s/
kubectl delete -f .
docker images | grep everyhook | awk '{print $3}' | xargs docker rmi