SHELL := /bin/bash
IMAGE := $(notdir $(CURDIR))
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

all: src_test build config test ## Runs config and test

clean:  ## Kills container and leaves image for this module
	@echo "Killing Container(s)"
	bash -c "COMPOSE_PROJECT_NAME=$(IMAGE) docker-compose -f docker-compose.yml down"
	bash -c "COMPOSE_PROJECT_NAME=$(IMAGE)-server docker-compose -f docker-compose.yml down"
	./gradlew clean
	rm -rf travis/jenkins/*.*

build:  ## Execs any IDEMPOTENT actions that need to occur before building the image
	./gradlew jpi jar

src_test:  ## Execs any IDEMPOTENT actions that need to occur before building the image
	./gradlew test

test:  ## Execs into the container, and runs inspec tests
	@echo "Wait for jenkins"
	bash -c 'sleep 20'
	@echo "Running Tests"
	bash -c "inspec exec test/inspec -t docker://$(IMAGE)"

config:  ## Build the application container
	@echo "Building application container"
	bash -c "COMPOSE_PROJECT_NAME=$(IMAGE) docker-compose -f docker-compose.yml up  --build -d"

status:  ## Lists the docker container for this module
	bash -c "docker ps --filter name=$(IMAGE)"

.DEFAULT_GOAL := all
.PHONY: src_test build config test clean
