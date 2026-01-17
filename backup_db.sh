#!/bin/bash
set -e

# Configuration
CONTAINER_NAME="calorieai-db"
DB_USER="postgres"
DB_NAME="calorieTracker"
BACKUP_DIR="./backups"

# Create backup directory
mkdir -p "$BACKUP_DIR"

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="$BACKUP_DIR/backup_$TIMESTAMP.sql"

echo "=========================================="
echo "      Backing up Database ($DB_NAME)      "
echo "=========================================="
echo "Container: $CONTAINER_NAME"
echo "Output:    $BACKUP_FILE"

# Run pg_dump inside the container
docker exec -t "$CONTAINER_NAME" pg_dump -U "$DB_USER" "$DB_NAME" > "$BACKUP_FILE"

if [ $? -eq 0 ]; then
    echo "✅ Backup Successful!"
    
    # Retention Policy: Delete backups older than 7 days
    echo "Cleaning up backups older than 7 days..."
    find "$BACKUP_DIR" -name "backup_*.sql" -type f -mtime +7 -exec rm {} \;
    echo "Cleanup complete."
else
    echo "❌ Backup Failed."
    rm -f "$BACKUP_FILE"
    exit 1
fi
