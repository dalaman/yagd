SHELL:=/bin/bash -O globstar

JAVA_FILES=$(shell ls ./**/*.java)
EXEC_FILE=SessionManager
SERVER_DIR=server


run: ${JAVA_FILES}
	@javac ${JAVA_FILES}
	@cd ${SERVER_DIR} && java ${EXEC_FILE}
	@echo

format: ${JAVA_FILES}
	@clang-format -i ${JAVA_FILES}

clean:
	@yes | rm -f $(shell ls ./**/*.class)

.PHONY: run format clean