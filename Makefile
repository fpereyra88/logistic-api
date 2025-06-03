# Makefile for building and running the logistic API with SQLite persistence

IMAGE_NAME=logistic-api
PORT=8080
DB_FILE=$(shell pwd)/logistic.db

.PHONY: build run clean

build:
	docker build -t $(IMAGE_NAME) .

run:
	docker build -t $(IMAGE_NAME) .
	docker run -p $(PORT):8080 -v $(DB_FILE):/app/logistic.db $(IMAGE_NAME)

clean:
	docker rmi $(IMAGE_NAME) || true

