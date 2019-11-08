HELM_REPOSITORY=https://artifactory.toolchain.lead.prod.liatr.io/artifactory/maven
VERSION=$(shell git describe --tags --dirty | cut -c 2-)

version:
	@echo "$(VERSION)"


test:
	@mvn test

build:
	@mvn -B versions:set -DnewVersion=$(VERSION)
	@mvn -B clean package
	@mvn -B versions:revert

run: 
	@mvn hpi:run

deploy:
	@curl -u $(ARTIFACTORY_CREDS) -X PUT "$(HELM_REPOSITORY)/pipeline-status-$(VERSION).hpi" -T target/pipeline-status-plugin.hpi

.PHONY: build test run
