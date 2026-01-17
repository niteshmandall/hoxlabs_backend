#!/bin/bash
set -e

# Configuration
CONTAINER_NAME="calorieai-db"
DB_USER="postgres"
DB_NAME="calorieTracker"

if [ -z "$1" ]; then
    echo "Usage: ./restore_db.sh <backup_file.sql>"
    exit 1
fi

BACKUP_FILE="$1"

if [ ! -f "$BACKUP_FILE" ]; then
    echo "Error: File '$BACKUP_FILE' not found."
    exit 1
fi

echo "=========================================="
echo "      Restoring Database ($DB_NAME)       "
echo "=========================================="
echo "Container: $CONTAINER_NAME"
echo "Input:     $BACKUP_FILE"
echo "⚠️  WARNING: This will overwrite current data. Press Ctrl+C to cancel."
echo "Waiting 5 seconds..."
sleep 5

# Drop and recreate (to ensure clean slate) or just run psql
# We'll pipe the file into psql
cat "$BACKUP_FILE" | docker exec -i "$CONTAINER_NAME" psql -U "$DB_USER" "$DB_NAME"

if [ $? -eq 0 ]; then
    echo "✅ Restore Successful!"
else
    echo "❌ Restore Failed."
    exit 1
fi
