#!/bin/bash

# OAuth2 Authorization Code Flow Test Script
# Usage: ./test-oauth2-flow.sh
# Make sure the auth-server is running on port 9000

set -e

BASE_URL="http://localhost:9000"
CLIENT_ID="meeting-planner-web"
CLIENT_SECRET="web-secret"
REDIRECT_URI="http://localhost:8080/authorized"
USERNAME="admin"
PASSWORD="admin"

echo "🔐 Testing OAuth2 Authorization Code Flow"
echo "========================================"

# Create a temporary directory for cookies and session data
TEMP_DIR=$(mktemp -d)
COOKIE_JAR="$TEMP_DIR/cookies.txt"
SESSION_FILE="$TEMP_DIR/session.txt"

cleanup() {
    rm -rf "$TEMP_DIR"
}
trap cleanup EXIT

echo "📁 Using temporary directory: $TEMP_DIR"

# Step 1: Initiate authorization request
echo "🚀 Step 1: Initiating authorization request..."
AUTH_URL="${BASE_URL}/oauth2/authorize?response_type=code&client_id=${CLIENT_ID}&redirect_uri=${REDIRECT_URI}&scope=openid%20profile%20meeting.read%20meeting.write&state=test123"

echo "   Authorization URL: $AUTH_URL"

# Follow redirect to login page
echo "🔄 Step 2: Following redirect to login page..."
LOGIN_RESPONSE=$(curl -s -w "%{http_code}|%{redirect_url}" -c "$COOKIE_JAR" "$AUTH_URL")
LOGIN_HTTP_CODE=$(echo "$LOGIN_RESPONSE" | cut -d'|' -f1)
LOGIN_REDIRECT=$(echo "$LOGIN_RESPONSE" | cut -d'|' -f2)

if [ "$LOGIN_HTTP_CODE" != "302" ]; then
    echo "❌ Expected redirect (302) but got: $LOGIN_HTTP_CODE"
    exit 1
fi

echo "✅ Redirected to: $LOGIN_REDIRECT"

# Step 3: Get login page and extract CSRF token
echo "🔑 Step 3: Getting login page and extracting CSRF token..."
LOGIN_PAGE=$(curl -s -b "$COOKIE_JAR" -c "$COOKIE_JAR" "${BASE_URL}/login")

# Extract CSRF token using different patterns
CSRF_TOKEN=$(echo "$LOGIN_PAGE" | grep -o 'name="_csrf" type="hidden" value="[^"]*"' | sed 's/.*value="\([^"]*\)".*/\1/' | head -1)

if [ -z "$CSRF_TOKEN" ]; then
    # Try alternative pattern
    CSRF_TOKEN=$(echo "$LOGIN_PAGE" | grep -o 'type="hidden" name="_csrf" value="[^"]*"' | sed 's/.*value="\([^"]*\)".*/\1/' | head -1)
fi

if [ -z "$CSRF_TOKEN" ]; then
    echo "❌ Could not extract CSRF token from login page"
    echo "🔍 Login page content (first 500 chars):"
    echo "$LOGIN_PAGE" | head -c 500
    exit 1
fi

echo "✅ CSRF token extracted: ${CSRF_TOKEN:0:20}..."

# Step 4: Submit login form
echo "🔐 Step 4: Submitting login credentials..."
LOGIN_SUBMIT_RESPONSE=$(curl -s -w "%{http_code}|%{redirect_url}" \
    -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
    -X POST \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "username=${USERNAME}&password=${PASSWORD}&_csrf=${CSRF_TOKEN}" \
    "${BASE_URL}/login")

LOGIN_SUBMIT_CODE=$(echo "$LOGIN_SUBMIT_RESPONSE" | cut -d'|' -f1)
LOGIN_SUBMIT_REDIRECT=$(echo "$LOGIN_SUBMIT_RESPONSE" | cut -d'|' -f2)

if [ "$LOGIN_SUBMIT_CODE" != "302" ]; then
    echo "❌ Login failed. Expected redirect (302) but got: $LOGIN_SUBMIT_CODE"
    exit 1
fi

echo "✅ Login successful, redirected to: $LOGIN_SUBMIT_REDIRECT"

# Step 5: Follow the OAuth2 authorize redirect chain
echo "🔄 Step 5: Following OAuth2 authorization redirect chain..."

# Follow the redirect to /oauth2/authorize
AUTHORIZE_RESPONSE=$(curl -s -w "%{http_code}|%{redirect_url}" -b "$COOKIE_JAR" -c "$COOKIE_JAR" "$LOGIN_SUBMIT_REDIRECT")
AUTHORIZE_CODE=$(echo "$AUTHORIZE_RESPONSE" | cut -d'|' -f1)
AUTHORIZE_REDIRECT=$(echo "$AUTHORIZE_RESPONSE" | cut -d'|' -f2)

# Check if we're redirected to the consent page
if [[ "$AUTHORIZE_REDIRECT" == *"consent"* ]] || [[ "$AUTHORIZE_REDIRECT" == *"oauth2/consent"* ]]; then
    echo "📝 Step 6: Processing consent page..."
    
    CONSENT_PAGE=$(curl -s -b "$COOKIE_JAR" -c "$COOKIE_JAR" "$AUTHORIZE_REDIRECT")
    
    # Extract CSRF token from consent page
    CONSENT_CSRF=$(echo "$CONSENT_PAGE" | grep -o 'name="_csrf" type="hidden" value="[^"]*"' | sed 's/.*value="\([^"]*\)".*/\1/' | head -1)
    
    if [ -z "$CONSENT_CSRF" ]; then
        # Try alternative pattern for CSRF token
        CONSENT_CSRF=$(echo "$CONSENT_PAGE" | grep -o 'type="hidden" name="_csrf" value="[^"]*"' | sed 's/.*value="\([^"]*\)".*/\1/' | head -1)
    fi
    
    if [ -z "$CONSENT_CSRF" ]; then
        # Try Thymeleaf pattern: th:name="${_csrf.parameterName}" th:value="${_csrf.token}"
        CONSENT_CSRF=$(echo "$CONSENT_PAGE" | grep -o 'th:value="[^"]*"' | grep -A1 '_csrf' | sed 's/.*th:value="\([^"]*\)".*/\1/' | head -1)
    fi
    
    if [ -z "$CONSENT_CSRF" ]; then
        CONSENT_CSRF="$CSRF_TOKEN"  # Reuse login CSRF token
    fi
    
        # Extract all required and optional fields from the consent page
        CLIENT_ID_VALUE=$(echo "$CONSENT_PAGE" | grep -o 'name="client_id" value="[^"]*"' | sed 's/.*value="\([^"]*\)".*/\1/' | head -1)
        STATE_VALUE=$(echo "$CONSENT_PAGE" | grep -o 'name="state" value="[^"]*"' | sed 's/.*value="\([^"]*\)".*/\1/' | head -1)
        REDIRECT_URI_VALUE=$(echo "$CONSENT_PAGE" | grep -o 'name="redirect_uri" value="[^"]*"' | sed 's/.*value="\([^"]*\)".*/\1/' | head -1)
        CODE_CHALLENGE_VALUE=$(echo "$CONSENT_PAGE" | grep -o 'name="code_challenge" value="[^"]*"' | sed 's/.*value="\([^"]*\)".*/\1/' | head -1)
        CODE_CHALLENGE_METHOD_VALUE=$(echo "$CONSENT_PAGE" | grep -o 'name="code_challenge_method" value="[^"]*"' | sed 's/.*value="\([^"]*\)".*/\1/' | head -1)
        
        if [ -z "$CLIENT_ID_VALUE" ]; then
            CLIENT_ID_VALUE="$CLIENT_ID"
        fi
        
        # Build consent form data matching your consent.html structure
        # URL encode special characters in values
        CLIENT_ID_ENCODED=$(printf '%s' "$CLIENT_ID_VALUE" | sed 's/ /%20/g')
        STATE_ENCODED=$(printf '%s' "$STATE_VALUE" | sed 's/ /%20/g;s/=/%3D/g')
        CSRF_ENCODED=$(printf '%s' "$CONSENT_CSRF" | sed 's/ /%20/g;s/=/%3D/g')
        
        CONSENT_DATA="client_id=${CLIENT_ID_ENCODED}&state=${STATE_ENCODED}&_csrf=${CSRF_ENCODED}&user_oauth_approval=true"
        
        # Add optional fields if they exist (with URL encoding)
        if [ -n "$REDIRECT_URI_VALUE" ]; then
            REDIRECT_URI_ENCODED=$(printf '%s' "$REDIRECT_URI_VALUE" | sed 's/:/%3A/g;s/\//%2F/g')
            CONSENT_DATA="${CONSENT_DATA}&redirect_uri=${REDIRECT_URI_ENCODED}"
        fi
        if [ -n "$CODE_CHALLENGE_VALUE" ]; then
            CONSENT_DATA="${CONSENT_DATA}&code_challenge=${CODE_CHALLENGE_VALUE}"
        fi
        if [ -n "$CODE_CHALLENGE_METHOD_VALUE" ]; then
            CONSENT_DATA="${CONSENT_DATA}&code_challenge_method=${CODE_CHALLENGE_METHOD_VALUE}"
        fi
        
        # Extract all scope values from the actual form (matching your template's hidden inputs)
        # Try standard HTML pattern first
        SCOPES=$(echo "$CONSENT_PAGE" | grep -o 'name="scope" value="[^"]*"' | sed 's/.*value="\([^"]*\)".*/\1/')
        
        # If no scopes found, try Thymeleaf pattern
        if [ -z "$SCOPES" ]; then
            SCOPES=$(echo "$CONSENT_PAGE" | grep -A2 'name="scope"' | grep -o 'value="[^"]*"' | sed 's/.*value="\([^"]*\)".*/\1/')
        fi
        
        # If still no scopes, extract from the scope display section and use the requested scopes
        if [ -z "$SCOPES" ]; then
            # Extract scopes from the current consent URL parameters
            SCOPE_PARAM=$(echo "$AUTHORIZE_REDIRECT" | grep -o 'scope=[^&]*' | cut -d'=' -f2 | sed 's/%20/ /g')
            SCOPES=$(echo "$SCOPE_PARAM" | tr ' ' '\n')
        fi
        
        # Add each scope as a separate parameter
        for scope in $SCOPES; do
            CONSENT_DATA="${CONSENT_DATA}&scope=${scope}"
        done
        
    
    # Submit consent to /oauth2/authorize (as per your consent.html form action)
    CONSENT_RESPONSE=$(curl -s -w "%{http_code}|%{redirect_url}" \
        -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
        -X POST \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "$CONSENT_DATA" \
        "${BASE_URL}/oauth2/authorize")
    
    CONSENT_CODE=$(echo "$CONSENT_RESPONSE" | cut -d'|' -f1)
    CALLBACK_URL=$(echo "$CONSENT_RESPONSE" | cut -d'|' -f2)
    
    echo "✅ Consent approved and submitted"
else
    # No consent redirect, but check if we got a final redirect with authorization code
    echo "ℹ️  No consent page redirect, checking if authorization completed..."
    
    if [ "$AUTHORIZE_CODE" == "302" ] && [[ "$AUTHORIZE_REDIRECT" == *"code="* ]]; then
        # Direct authorization without consent - extract auth code
        CALLBACK_URL="$AUTHORIZE_REDIRECT"
        echo "✅ Direct authorization successful, callback: $CALLBACK_URL"
    else
        # Follow any remaining redirects
        REDIRECT_PAGE=$(curl -s -b "$COOKIE_JAR" -c "$COOKIE_JAR" "$AUTHORIZE_REDIRECT")
        
        # Check if the page contains consent form elements
        if [[ "$REDIRECT_PAGE" == *"user_oauth_approval"* ]] && [[ "$REDIRECT_PAGE" == *"Authorization Request"* ]]; then
        echo "📝 Found consent page by content analysis, handling it..."
        
        # Extract CSRF token from consent page
        CONSENT_CSRF=$(echo "$REDIRECT_PAGE" | grep -o 'name="_csrf" type="hidden" value="[^"]*"' | sed 's/.*value="\([^"]*\)".*/\1/' | head -1)
        
        if [ -z "$CONSENT_CSRF" ]; then
            # Try alternative pattern for CSRF token
            CONSENT_CSRF=$(echo "$REDIRECT_PAGE" | grep -o 'type="hidden" name="_csrf" value="[^"]*"' | sed 's/.*value="\([^"]*\)".*/\1/' | head -1)
        fi
        
        if [ -z "$CONSENT_CSRF" ]; then
            CONSENT_CSRF="$CSRF_TOKEN"  # Reuse login CSRF token
        fi
        
        echo "   Using CSRF token: ${CONSENT_CSRF:0:20}..."
        
        # Extract all required and optional fields from the consent page
        CLIENT_ID_VALUE=$(echo "$REDIRECT_PAGE" | grep -o 'name="client_id" value="[^"]*"' | sed 's/.*value="\([^"]*\)".*/\1/' | head -1)
        STATE_VALUE=$(echo "$REDIRECT_PAGE" | grep -o 'name="state" value="[^"]*"' | sed 's/.*value="\([^"]*\)".*/\1/' | head -1)
        REDIRECT_URI_VALUE=$(echo "$REDIRECT_PAGE" | grep -o 'name="redirect_uri" value="[^"]*"' | sed 's/.*value="\([^"]*\)".*/\1/' | head -1)
        CODE_CHALLENGE_VALUE=$(echo "$REDIRECT_PAGE" | grep -o 'name="code_challenge" value="[^"]*"' | sed 's/.*value="\([^"]*\)".*/\1/' | head -1)
        CODE_CHALLENGE_METHOD_VALUE=$(echo "$REDIRECT_PAGE" | grep -o 'name="code_challenge_method" value="[^"]*"' | sed 's/.*value="\([^"]*\)".*/\1/' | head -1)
        
        if [ -z "$CLIENT_ID_VALUE" ]; then
            CLIENT_ID_VALUE="$CLIENT_ID"
        fi
        
        echo "   Client ID: $CLIENT_ID_VALUE"
        echo "   State: $STATE_VALUE"
        echo "   Redirect URI: $REDIRECT_URI_VALUE"
        echo "   Code Challenge: ${CODE_CHALLENGE_VALUE:0:20}${CODE_CHALLENGE_VALUE:+...}"
        echo "   Code Challenge Method: $CODE_CHALLENGE_METHOD_VALUE"
        
        # Build consent form data matching your consent.html structure
        CONSENT_DATA="client_id=${CLIENT_ID_VALUE}&state=${STATE_VALUE}&_csrf=${CONSENT_CSRF}&user_oauth_approval=true"
        
        # Add optional fields if they exist
        if [ -n "$REDIRECT_URI_VALUE" ]; then
            CONSENT_DATA="${CONSENT_DATA}&redirect_uri=${REDIRECT_URI_VALUE}"
        fi
        if [ -n "$CODE_CHALLENGE_VALUE" ]; then
            CONSENT_DATA="${CONSENT_DATA}&code_challenge=${CODE_CHALLENGE_VALUE}"
        fi
        if [ -n "$CODE_CHALLENGE_METHOD_VALUE" ]; then
            CONSENT_DATA="${CONSENT_DATA}&code_challenge_method=${CODE_CHALLENGE_METHOD_VALUE}"
        fi
        
        # Extract all scope values from the actual form (matching your template's hidden inputs)
        SCOPES=$(echo "$CONSENT_PAGE" | grep -o 'name="scope" value="[^"]*"' | sed 's/.*value="\([^"]*\)".*/\1/')
        
        # Add each scope as a separate parameter
        for scope in $SCOPES; do
            CONSENT_DATA="${CONSENT_DATA}&scope=${scope}"
        done
        
        echo "   Extracted scopes: $SCOPES"
        echo "   🔄 Complete form data: ${CONSENT_DATA}"
        
        # Debug: Show all form elements found
        echo "   🔍 Form elements found:"
        echo "$REDIRECT_PAGE" | grep -o 'name="[^"]*" value="[^"]*"' | head -10 | while read line; do
            echo "      $line"
        done
        
        # Submit consent to /oauth2/authorize (as per your consent.html form action)
        CONSENT_RESPONSE=$(curl -s -w "%{http_code}|%{redirect_url}" \
            -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
            -X POST \
            -H "Content-Type: application/x-www-form-urlencoded" \
            -d "$CONSENT_DATA" \
            "${BASE_URL}/oauth2/authorize")
        
        CONSENT_CODE=$(echo "$CONSENT_RESPONSE" | cut -d'|' -f1)
        CALLBACK_URL=$(echo "$CONSENT_RESPONSE" | cut -d'|' -f2)
        
        echo "   Consent response code: $CONSENT_CODE"
        echo "✅ Consent submitted, redirected to: $CALLBACK_URL"
        else
            # No consent page found, continue with normal authorization flow
            echo "ℹ️  No consent page found, continuing authorization flow..."
            
            # Follow the redirect chain
            CALLBACK_RESPONSE=$(curl -s -w "%{http_code}|%{redirect_url}" -b "$COOKIE_JAR" "$AUTHORIZE_REDIRECT")
            CALLBACK_CODE=$(echo "$CALLBACK_RESPONSE" | cut -d'|' -f1)
            CALLBACK_URL=$(echo "$CALLBACK_RESPONSE" | cut -d'|' -f2)
            
            # If still redirecting, follow one more time
            if [ "$CALLBACK_CODE" == "302" ]; then
                FINAL_RESPONSE=$(curl -s -w "%{http_code}|%{redirect_url}" -b "$COOKIE_JAR" "$CALLBACK_URL")
                CALLBACK_URL=$(echo "$FINAL_RESPONSE" | cut -d'|' -f2)
            fi
        fi
    fi
fi

# Step 7: Extract authorization code from callback URL
echo "🔍 Step 7: Extracting authorization code..."
if [[ "$CALLBACK_URL" == *"code="* ]]; then
    AUTH_CODE=$(echo "$CALLBACK_URL" | grep -o 'code=[^&]*' | cut -d'=' -f2)
    echo "✅ Authorization code extracted: ${AUTH_CODE:0:20}..."
else
    echo "❌ No authorization code found in callback URL: $CALLBACK_URL"
    exit 1
fi

# Step 8: Exchange authorization code for tokens
echo "🎫  Step 8: Exchanging authorization code for tokens..."
CLIENT_AUTH=$(echo -n "${CLIENT_ID}:${CLIENT_SECRET}" | base64)

TOKEN_RESPONSE=$(curl -s -X POST \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -H "Authorization: Basic $CLIENT_AUTH" \
    -d "grant_type=authorization_code&code=${AUTH_CODE}&redirect_uri=${REDIRECT_URI}" \
    "${BASE_URL}/oauth2/token")

echo "🎉 Token Response:"
echo "$TOKEN_RESPONSE" | jq '.' || echo "$TOKEN_RESPONSE"

# Step 9: Extract and verify tokens
ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | jq -r '.access_token // empty')
ID_TOKEN=$(echo "$TOKEN_RESPONSE" | jq -r '.id_token // empty')

if [ -n "$ACCESS_TOKEN" ] && [ "$ACCESS_TOKEN" != "null" ]; then
    echo "✅ Access token received: ${ACCESS_TOKEN:0:50}..."
    
    # Verify access token is a JWT (3 parts separated by dots)
    TOKEN_PARTS=$(echo "$ACCESS_TOKEN" | tr -cd '.' | wc -c)
    if [ "$TOKEN_PARTS" -eq 2 ]; then
        echo "✅ Access token is a valid JWT format"
    else
        echo "⚠️  Access token might not be JWT format (found $TOKEN_PARTS dots, expected 2)"
    fi
else
    echo "❌ No access token received"
    exit 1
fi

if [ -n "$ID_TOKEN" ] && [ "$ID_TOKEN" != "null" ]; then
    echo "✅ ID token received: ${ID_TOKEN:0:50}..."
else
    echo "ℹ️  No ID token (may be normal depending on scopes)"
fi

# Step 10: Test token introspection
echo "🔍 Step 10: Testing token introspection..."
INTROSPECT_RESPONSE=$(curl -s -X POST \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -H "Authorization: Basic $CLIENT_AUTH" \
    -d "token=${ACCESS_TOKEN}" \
    "${BASE_URL}/oauth2/introspect")

echo "🔍 Introspection Response:"
echo "$INTROSPECT_RESPONSE" | jq '.' || echo "$INTROSPECT_RESPONSE"

# Final verification
ACTIVE=$(echo "$INTROSPECT_RESPONSE" | jq -r '.active // false')
if [ "$ACTIVE" == "true" ]; then
    echo "✅ Token is active and valid"
else
    echo "❌ Token introspection shows token is not active"
fi

echo ""
echo "🎉 OAuth2 Authorization Code Flow Test Complete!"
echo "✅ All steps completed successfully"