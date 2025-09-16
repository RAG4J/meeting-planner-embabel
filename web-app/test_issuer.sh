#!/bin/bash

# Test script to verify auth-server issuer configuration

echo "🔍 Testing Auth Server Issuer Configuration"
echo "==========================================="
echo

# Test 1: Check if auth-server hostname resolves
echo "1. Testing hostname resolution:"
if ping -c 1 auth-server >/dev/null 2>&1; then
    echo "✅ auth-server hostname resolves correctly"
else
    echo "❌ auth-server hostname does not resolve"
    echo "   Make sure you have 'auth-server' in /etc/hosts pointing to 127.0.0.1"
    exit 1
fi
echo

# Test 2: Check if auth-server is running
echo "2. Testing auth-server availability:"
if curl -s --connect-timeout 5 http://auth-server:9000/actuator/health >/dev/null 2>&1; then
    echo "✅ Auth-server is responding on port 9000"
else
    echo "❌ Auth-server is not responding on port 9000"
    echo "   Make sure auth-server is running: cd auth-server && mvn spring-boot:run"
    exit 1
fi
echo

# Test 3: Check OpenID Connect configuration
echo "3. Testing OpenID Connect discovery:"
ISSUER=$(curl -s http://auth-server:9000/.well-known/openid-configuration 2>/dev/null | grep -o '"issuer":"[^"]*"' | cut -d'"' -f4)

if [[ -n "$ISSUER" ]]; then
    echo "✅ OpenID Connect configuration found"
    echo "   Issuer: $ISSUER"
    
    if [[ "$ISSUER" == "http://auth-server:9000" ]]; then
        echo "✅ Issuer is correctly configured as http://auth-server:9000"
    else
        echo "❌ Issuer mismatch!"
        echo "   Expected: http://auth-server:9000"
        echo "   Found: $ISSUER"
        echo "   This will cause the OAuth2 error you're seeing."
    fi
else
    echo "❌ Could not retrieve issuer from OpenID Connect configuration"
fi
echo

# Test 4: Show full configuration
echo "4. Full OpenID Connect Configuration:"
curl -s http://auth-server:9000/.well-known/openid-configuration 2>/dev/null | python3 -m json.tool || echo "Could not format JSON"
echo

echo "==========================================="
echo "Test complete!"