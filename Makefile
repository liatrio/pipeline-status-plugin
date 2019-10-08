test:
	@gradle clean build

build:
	@gradle clean assemble

run: 
	@gradle clean server

deploy:
	@gradle clean build githubRelease

.PHONY: build test run
