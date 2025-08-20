# Define variables
MIGRATION_DIR=src/main/resources/db/migration
FILENAME_PREFIX=V
FILENAME_SUFFIX=.sql

# Get the current timestamp in Flyway's format (YYYYMMDDHHMMSS)
TIMESTAMP=$(shell date +"%Y%m%d%H%M%S")

# Usage: make migration NAME=YourMigrationName
migration:
	@echo "Generating Flyway migration file..."
	@if [ -z "$(NAME)" ]; then \
		echo "Error: NAME is required. Usage: make migration NAME=your_migration_file_name"; \
		exit 1; \
	fi
	@touch $(MIGRATION_DIR)/$(FILENAME_PREFIX)$(TIMESTAMP)__$(NAME)$(FILENAME_SUFFIX)
	@echo "Created $(MIGRATION_DIR)/$(FILENAME_PREFIX)$(TIMESTAMP)__$(NAME)$(FILENAME_SUFFIX)"