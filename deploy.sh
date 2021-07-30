mvn clean package
consumerimg=localhost:5000/everyhook/badger-consumer
providerimg=localhost:5000/everyhook/badger-provider
backendimg=localhost:5000/everyhook/badger-backend
LOCAL=$(PWD)
echo "$LOCAL"
docker images | grep everyhook | awk '{print $3}' | xargs docker rmi
cd badger-example/k8s/
kubectl delete -f .
cd "$LOCAL"

echo 'build consumer'
cd ./badger-example/consumer/
docker build -t $consumerimg .
cd "$LOCAL"

echo 'build provider'
cd ./badger-example/provider
docker build -t $providerimg .
cd "$LOCAL"

echo 'build backend'
cd ./badger-example/backend
docker build -t $backendimg .
cd "$LOCAL"

echo 'push consumer'
docker push $consumerimg
echo 'push provider'
docker push $providerimg
echo 'push backend'
docker push $backendimg
echo 'deploy k8s'
cd badger-example/k8s/
kubectl create -f .
