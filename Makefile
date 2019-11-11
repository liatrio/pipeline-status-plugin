verify:
	@mvn -B clean verify

package:
	@mvn -B clean package

run: 
	@mvn hpi:run

deploy:
	@mvn -B clean deploy

.PHONY: verify package run deploy
