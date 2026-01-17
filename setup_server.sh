#!/bin/bash

# setup_server.sh
# Automates the setup of a secure local server environment.
# Run with: sudo ./setup_server.sh <CLOUDFLARE_TOKEN>

set -e

echo "Starting Server Setup..."

# 1. Update System
echo "[1/4] Updating system packages..."
apt update && apt upgrade -y

# 2. Security Basics (UFW & Fail2Ban)
echo "[2/4] Installing and configuring UFW & Fail2Ban..."
apt install -y ufw fail2ban unattended-upgrades

# Configure UFW
ufw allow ssh
# Note: We do NOT open port 8080, as we will use Cloudflare Tunnel
# If you need local access for debugging, uncomment the next line:
# ufw allow 8080/tcp 

echo "Enabling Firewall (UFW)..."
ufw --force enable

# Configure Unattended Upgrades
echo "Configuring Unattended Upgrades..."
dpkg-reconfigure --priority=low unattended-upgrades

# 3. Power Management (Prevent Sleep)
echo "[3/4] Tuning power management (prevent sleep on lid close)..."
# We backup the config
cp /etc/systemd/logind.conf /etc/systemd/logind.conf.bak
# Ensure HandleLidSwitch is ignore
sed -i 's/^#HandleLidSwitch=.*/HandleLidSwitch=ignore/' /etc/systemd/logind.conf
sed -i 's/^HandleLidSwitch=.*/HandleLidSwitch=ignore/' /etc/systemd/logind.conf

echo "Restarting logind service..."
systemctl restart systemd-logind

# 4. Install Docker
echo "[4/4] Installing Docker..."
if ! command -v docker &> /dev/null; then
    # Add Docker's official GPG key and repository
    apt-get install -y ca-certificates curl gnupg
    install -m 0755 -d /etc/apt/keyrings
    if [ ! -f /etc/apt/keyrings/docker.gpg ]; then
        curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
        chmod a+r /etc/apt/keyrings/docker.gpg
    fi

    # Add the repository to Apt sources
    echo \
      "deb [arch=\"$(dpkg --print-architecture)\" signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
      \"$(. /etc/os-release && echo \"$VERSION_CODENAME\")\" stable" | \
      tee /etc/apt/sources.list.d/docker.list > /dev/null
    
    apt-get update
    apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
    
    # Add current user to docker group (if sudo is run by user)
    if [ -n "$SUDO_USER" ]; then
        usermod -aG docker "$SUDO_USER"
        echo "Added user $SUDO_USER to docker group."
    fi
else
    echo "Docker is already installed."
fi

# 5. Install Cloudflare Tunnel (cloudflared)
echo "[5/5] Installing Cloudflare Tunnel (cloudflared)..."
# Add cloudflare gpg key
mkdir -p --mode=0755 /usr/share/keyrings
curl -fsSL https://pkg.cloudflare.com/cloudflare-public-v2.gpg | tee /usr/share/keyrings/cloudflare-public-v2.gpg >/dev/null

# Add this repo to your apt repositories
echo 'deb [signed-by=/usr/share/keyrings/cloudflare-public-v2.gpg] https://pkg.cloudflare.com/cloudflared any main' | tee /etc/apt/sources.list.d/cloudflared.list

# install cloudflared
apt-get update && apt-get install -y cloudflared

# 6. Connect Tunnel (Service Mode)
if [ -z "$1" ]; then
    echo "⚠️  No Cloudflare Token provided."
    echo "   To connect automatically run: sudo ./setup_server.sh <YOUR_TOKEN>"
    echo "   Or connect manually later: sudo cloudflared service install <YOUR_TOKEN>"
else
    echo "[6/6] Connecting to Cloudflare Tunnel..."
    cloudflared service install "$1" || echo "Cloudflare tunnel service already installed."
    systemctl start cloudflared || echo "Could not start cloudflared service."
fi

echo "=========================================="
echo "Setup Complete!"
echo "Please log out and log back in for docker group changes to take effect."
echo "Your server should now be live behind Cloudflare Zero Trust."
echo "=========================================="
