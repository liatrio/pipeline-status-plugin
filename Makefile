.PHONY: test
test:
	docker build -t jenkins-tester -f test/Dockerfile .
	docker run -p 8080:8080 jenkins-tester

.PHONY: build
build:
	@gradle clean jpi

.PHONY: run
run: build test
