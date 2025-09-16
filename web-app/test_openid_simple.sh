#!/bin/bash

echo "🔍 Testing OpenID Connect Discovery"
echo "=================================="
echo

# Test if auth-server is running
echo "1. Testing auth-server basic connectivity..."
if curl -s --connect-timeout 5 http://auth-server:9000/actuator/health >/dev/null 2>&1; then
    echo "✅ Auth-server is responding"
else
    echo "❌ Auth-server is not responding"
    echo "   Make sure it's running: cd auth-server && mvn spring-boot:run"
    exit 1
fi

echo
echo "2. Testing OpenID Connect discovery endpoint..."
echo "   GET http://auth-server:9000/.well-known/openid-configuration"
echo

# Get the response with headers
RESPONSE=$(curl -s -w "HTTP_CODE:%{http_code}" http://auth-server:9000/.well-known/openid-configuration)
HTTP_CODE=$(echo "$RESPONSE" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)
BODY=$(echo "$RESPONSE" | sed 's/HTTP_CODE:[0-9]*$//')

echo "   HTTP Status: $HTTP_CODE"

if [[ "$HTTP_CODE" == "200" ]]; then
    echo "✅ OpenID Connect discovery endpoint is working!"
    
    # Try to extract and validate the issuer
    if command -v python3 >/dev/null 2>&1; then
        ISSUER=$(echo "$BODY" | python3 -c "import sys, json; print(json.load(sys.stdin).get('issuer', 'NOT_FOUND'))" 2>/dev/null || echo "PARSE_ERROR")
        
        if [[ "$ISSUER" == "http://auth-server:9000" ]]; then
            echo "✅ Issuer is correctly set: $ISSUER"
        elif [[ "$ISSUER" == "PARSE_ERROR" ]]; then
            echo "⚠️  Could not parse JSON response"
        elif [[ "$ISSUER" == "NOT_FOUND" ]]; then
            echo "❌ Issuer field not found in response"
        else
            echo "❌ Issuer mismatch: Expected 'http://auth-server:9000', got '$ISSUER'"
        fi
    else
        echo "ℹ️  Python3 not available for JSON parsing"
    fi
    
    echo
    echo "📋 OpenID Connect Configuration:"
    echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"
    
elif [[ "$HTTP_CODE" == "404" ]]; then
    echo "❌ OpenID Connect discovery endpoint not found (404)"
    echo "   This means the OAuth2 Authorization Server isn't properly configured"
    
elif [[ "$HTTP_CODE" == "302" ]]; then
    echo "❌ Discovery endpoint is redirecting (302) - it should be publicly accessible"
    echo "   This indicates a security configuration issue"
    
else
    echo "❌ Unexpected HTTP status code: $HTTP_CODE"
    echo "   Response body: $BODY"
fi

echo
echo "=================================="
echo "Test complete!"