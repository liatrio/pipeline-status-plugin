test:
	@mvn test

build:
	@mvn package

run: 
	@mvn hpi:run

.PHONY: build test run
