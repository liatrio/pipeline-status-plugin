test:
	docker build -t jenkins-tester .
	docker run -p 8080:8080 jenkins-tester

build:
	@gradle clean jpi

run: build test

.PHONY: build test run
