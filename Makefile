HELM_REPOSITORY=https://artifactory.toolchain.lead.prod.liatr.io/artifactory/maven
VERSION=$(shell git describe --tags --dirty | cut -c 2-)

version:
	@echo "$(VERSION)"


test:
	@mvn test

build:
	@mvn clean package

run: 
	@mvn hpi:run

deploy:
	@curl -u $(ARTIFACTORY_CREDS) -X PUT "$(HELM_REPOSITORY)/pipeline-status-$(VERSION).hpi" -T /Users/jordana/liatrio/pipeline-status-plugin/target/pipeline-status-plugin.hpi

.PHONY: build test run
