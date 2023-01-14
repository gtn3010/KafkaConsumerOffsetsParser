
push:
  DOCKER_BUILDKIT=1 docker build -t docker-registry.g-pay.vn/platform-applications/kafka-read-offset:v1.0 .
  docker push docker-registry.g-pay.vn/platform-applications/kafka-read-offset:v1.0