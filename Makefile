
REG ?= registry.tce.com

PROJECT_NAME := femas

APP_NAME := all

TAG = $(shell git rev-parse --short HEAD)

PORT ?= 8708

VERSION ?= $(TAG)

build-image:
	docker build -f Dockerfile -t ${REG}/${PROJECT_NAME}/${APP_NAME}:${VERSION} .

push-image:
	docker push ${REG}/${PROJECT_NAME}/${APP_NAME}:${VERSION}

run-image:
	docker run -d --name femas-runner -w /usr/local/src/femas \
	-p ${PORT}:8080 \
	-e VERSION=${VERSION} \
	-v $(shell pwd)/logs:/usr/local/src/femas/femas-admin-starter/target/femas-admin-starter-0.3.0-${VERSION}/femas-admin/logs ${REG}/${PROJECT_NAME}/${APP_NAME}:${VERSION}

run-helm:
	helm install femas-helm ./femas-helm

reset-helm:
	helm uninstall femas-helm
