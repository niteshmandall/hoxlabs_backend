#!/bin/bash
set -e

echo "=========================================="
echo "      Setting up Local Tunnel Config      "
echo "=========================================="

# 1. Login if cert missing
if [ ! -f ~/.cloudflared/cert.pem ]; then
    echo "Logs showing no certificate. You need to authenticate."
    echo "A link will be generated. Copy it to your browser to authorize."
    cloudflared tunnel login
fi

# 2. Define Variables
TUNNEL_NAME="linux-laptop-local"
CONFIG_FILE=~/.cloudflared/config.yml
CRED_FILE_PATTERN=~/.cloudflared/*.json

# 3. Create Tunnel (Idempotent)
echo "Enter the domain you want to use (e.g., api.yourdomain.com):"
read DOMAIN

echo "Creating tunnel '$TUNNEL_NAME'..."
cloudflared tunnel create $TUNNEL_NAME || echo "Tunnel might already exist, proceeding..."

# Get Tunnel ID
# We find the specific JSON file for this tunnel to get the ID, or list it
# Simpler: just route to the tunnel name (Cloudflare handles ID resolution via name usually, but config needs ID)
# Let's grep the ID from the output or list
TUNNEL_ID=$(cloudflared tunnel list | grep $TUNNEL_NAME | awk '{print $1}')

if [ -z "$TUNNEL_ID" ]; then
    echo "Error: Could not find Tunnel ID for $TUNNEL_NAME"
    exit 1
fi

CRED_FILE=~/.cloudflared/${TUNNEL_ID}.json

echo "Routing $DOMAIN to this tunnel..."
cloudflared tunnel route dns $TUNNEL_NAME $DOMAIN || echo "Route might already exist or failed."

# 4. Generate Config
echo "Generating config.yml..."
cat > $CONFIG_FILE <<EOF
tunnel: $TUNNEL_ID
credentials-file: $CRED_FILE

ingress:
  - hostname: $DOMAIN
    service: http://localhost:8080
  - service: http_status:404
EOF

# 5. Run as Service (Local Config Mode)
# We need to stop the token-based service if it's running
echo "Installing Service..."
sudo cloudflared service uninstall || true
sudo cloudflared service install || true
sudo systemctl start cloudflared

echo "=========================================="
echo "Tunnel Configured Locally!"
echo "Configuration file: $CONFIG_FILE"
echo "URL: https://$DOMAIN"
echo "=========================================="
