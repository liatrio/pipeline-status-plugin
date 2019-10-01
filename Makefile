DOCKER_IMAGE="maven:3.6-jdk-8-slim"

.PHONY: build
build:
	@docker run -v ~/.m2/:/root/.m2/ -v $(shell pwd):/root/src/ -w /root/src ${DOCKER_IMAGE} /bin/bash -c "mvn package"

.PHONY: test
test:
	docker build --no-cache -t jenkins-tester .
	docker run -p 8080:8080 jenkins-tester
