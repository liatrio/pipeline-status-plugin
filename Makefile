MAVEN_IMAGE="maven:3.6-jdk-8-slim"

# Note that build and clean steps run from docker are much slower than when maven is run from the host system.
# If maven is configured on the host system then use that

.PHONY: build
build:
	@docker run -v ~/.m2/:/root/.m2/ -v $(shell pwd):/root/src/ -w /root/src ${MAVEN_IMAGE} /bin/bash -c "mvn package"

.PHONY: clean
clean:
	@docker run -v ~/.m2/:/root/.m2/ -v $(shell pwd):/root/src/ -w /root/src ${MAVEN_IMAGE} /bin/bash -c "mvn clean"

.PHONY: test
test:
	docker build -t jenkins-tester -f test/Dockerfile .
	docker run -p 8080:8080 jenkins-tester

.PHONY: localmvn
localmvn:
	@mvn clean package

.PHONY: run
run: localmvn test
