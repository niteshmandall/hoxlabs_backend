#!/bin/bash
set -e

# Config
BACKUP_SCRIPT="$(pwd)/backup_db.sh"
CRON_SCHEDULE="0 2 * * *" # Every day at 2:00 AM

echo "=========================================="
echo "      Setting up Daily Backup Cron        "
echo "=========================================="

if [ ! -f "$BACKUP_SCRIPT" ]; then
    echo "Error: Backup script not found at $BACKUP_SCRIPT"
    exit 1
fi

# Ensure script is executable
chmod +x "$BACKUP_SCRIPT"

# Check if job already exists
CRON_JOB="$CRON_SCHEDULE $BACKUP_SCRIPT >> $(pwd)/backup_cron.log 2>&1"
EXISTING_CRON=$(crontab -l 2>/dev/null || true)

if echo "$EXISTING_CRON" | grep -Fq "$BACKUP_SCRIPT"; then
    echo "⚠️  Cron job already exists. Skipping."
else
    # Install new cron job
    (echo "$EXISTING_CRON"; echo "$CRON_JOB") | crontab -
    echo "✅ Cron job added successfully."
    echo "Schedule: $CRON_SCHEDULE (Daily at 2 AM)"
    echo "Log File: $(pwd)/backup_cron.log"
fi

echo "=========================================="
echo "Current Cron Jobs:"
crontab -l
echo "=========================================="
