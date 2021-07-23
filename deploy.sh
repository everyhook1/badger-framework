mvn clean package
consumerimg=localhost:5000/everyhook/badger-consumer
providerimg=localhost:5000/everyhook/badger-provider
LOCAL=$(PWD)
echo "$LOCAL"
docker images | grep everyhook | awk '{print $3}' | xargs docker rmi
cd badger-example/k8s/
kubectl delete -f consumer.yaml
kubectl delete -f provider.yaml
cd "$LOCAL"
echo 'build consumer'
cd ./badger-example/consumer/
docker build -t $consumerimg .
cd "$LOCAL"
cd ./badger-example/provider
echo 'build provider'
docker build -t $providerimg .
cd "$LOCAL"
echo 'push consumer'
docker push $consumerimg
echo 'push provider'
docker push $providerimg
echo 'deploy k8s'
cd badger-example/k8s/
kubectl create -f consumer.yaml
kubectl create -f provider.yaml
