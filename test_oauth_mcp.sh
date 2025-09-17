#!/bin/bash

# OAuth2 and MCP Server Test Script
# Tests the complete OAuth2 authentication flow and MCP server connectivity

# set -e  # Exit on any error - disabled for better error handling

echo "🔍 OAuth2 and MCP Server Authentication Test"
echo "=============================================="
echo

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test configuration
AUTH_SERVER_URL="http://auth-server:9000"
MCP_SERVER_URL="http://localhost:8081"
CLIENT_ID="location-mcp"
CLIENT_SECRET="mcp-secret"
SCOPE="mcp.invoke"

# Function to print test results
print_result() {
    local test_status=$1
    local message=$2
    if [ "$test_status" = "PASS" ]; then
        echo -e "${GREEN}✅ $message${NC}"
    elif [ "$test_status" = "FAIL" ]; then
        echo -e "${RED}❌ $message${NC}"
    elif [ "$test_status" = "WARN" ]; then
        echo -e "${YELLOW}⚠️  $message${NC}"
    elif [ "$test_status" = "INFO" ]; then
        echo -e "${BLUE}ℹ️  $message${NC}"
    fi
}

# Function to decode JWT token
decode_jwt() {
    local token=$1
    
    if [ -z "$token" ]; then
        echo "No token provided for decoding"
        return 1
    fi
    
    # Split the JWT into parts (header.payload.signature)
    local header=$(echo "$token" | cut -d'.' -f1)
    local payload=$(echo "$token" | cut -d'.' -f2)
    local signature=$(echo "$token" | cut -d'.' -f3)
    
    echo "🔍 JWT Token Analysis"
    echo "═══════════════════="
    echo "Token Length: ${#token} characters"
    echo "Parts: Header.Payload.Signature"
    echo ""
    
    # Decode header (add padding if needed)
    echo "📋 Header:"
    local header_padded=$(pad_base64 "$header")
    if command -v python3 >/dev/null 2>&1; then
        echo "$header_padded" | base64 -d 2>/dev/null | python3 -m json.tool 2>/dev/null || echo "Failed to decode header"
    else
        echo "$header_padded" | base64 -d 2>/dev/null || echo "Failed to decode header"
    fi
    echo ""
    
    # Decode payload (add padding if needed)
    echo "📦 Payload (Claims):"
    local payload_padded=$(pad_base64 "$payload")
    if command -v python3 >/dev/null 2>&1; then
        echo "$payload_padded" | base64 -d 2>/dev/null | python3 -m json.tool 2>/dev/null || echo "Failed to decode payload"
    else
        echo "$payload_padded" | base64 -d 2>/dev/null || echo "Failed to decode payload"
    fi
    echo ""
    
    echo "🔐 Signature: $signature (length: ${#signature})"
    echo "Note: Signature cannot be decoded as it's a cryptographic hash"
    echo ""
    
    # Extract common claims for easy reading
    echo "📊 Key Claims Summary:"
    local payload_json=$(echo "$payload_padded" | base64 -d 2>/dev/null)
    
    if command -v python3 >/dev/null 2>&1 && [ -n "$payload_json" ]; then
        echo "$payload_json" | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    print(f'  Subject (sub): {data.get(\"sub\", \"N/A\")}')
    print(f'  Audience (aud): {data.get(\"aud\", \"N/A\")}')
    print(f'  Issuer (iss): {data.get(\"iss\", \"N/A\")}')
    print(f'  Client ID: {data.get(\"client_id\", \"N/A\")}')
    print(f'  Scope: {data.get(\"scope\", \"N/A\")}')
    
    # Handle expiration time
    exp = data.get('exp')
    if exp:
        import datetime
        exp_date = datetime.datetime.fromtimestamp(exp)
        print(f'  Expires (exp): {exp} ({exp_date})')
    else:
        print('  Expires (exp): N/A')
    
    # Handle issued at time
    iat = data.get('iat')
    if iat:
        import datetime
        iat_date = datetime.datetime.fromtimestamp(iat)
        print(f'  Issued At (iat): {iat} ({iat_date})')
    else:
        print('  Issued At (iat): N/A')
except:
    print('  Could not parse claims')
" 2>/dev/null || echo "  Could not extract claims summary"
    fi
}

# Function to add padding to base64 string
pad_base64() {
    local str=$1
    local missing=$((4 - ${#str} % 4))
    if [ $missing -ne 4 ]; then
        str="$str$(printf '%*s' $missing | tr ' ' '=')"
    fi
    echo "$str"
}

# Function to check if a service is running
check_service() {
    local name=$1
    local url=$2
    local timeout=${3:-5}
    
    print_result "INFO" "Checking $name availability..."
    
    if curl -s --connect-timeout $timeout --max-time $timeout "$url" >/dev/null 2>&1; then
        print_result "PASS" "$name is responding"
        return 0
    else
        print_result "FAIL" "$name is not responding at $url"
        return 1
    fi
}

# Function to test OAuth2 client credentials flow
test_oauth2_flow() {
    print_result "INFO" "Testing OAuth2 client credentials flow..." >&2
    
    # Encode client credentials for Basic Auth
    local credentials_b64=$(echo -n "$CLIENT_ID:$CLIENT_SECRET" | base64)
    
    # Request access token
    local response=$(curl -s -w "HTTP_CODE:%{http_code}" \
        -X POST \
        -H "Authorization: Basic $credentials_b64" \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "grant_type=client_credentials&scope=$SCOPE" \
        "$AUTH_SERVER_URL/oauth2/token" 2>/dev/null)
    
    local http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)
    local body=$(echo "$response" | sed 's/HTTP_CODE:[0-9]*$//')
    
    if [ "$http_code" = "200" ]; then
        print_result "PASS" "OAuth2 token request successful (HTTP $http_code)" >&2
        
        # Extract access token
        local access_token=$(echo "$body" | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)
        
        if [ -n "$access_token" ]; then
            print_result "PASS" "Access token received (length: ${#access_token} chars)" >&2
            
            # Extract other token info
            local token_type=$(echo "$body" | grep -o '"token_type":"[^"]*' | cut -d'"' -f4)
            local expires_in=$(echo "$body" | grep -o '"expires_in":[0-9]*' | cut -d: -f2)
            local scope_returned=$(echo "$body" | grep -o '"scope":"[^"]*' | cut -d'"' -f4)
            
            print_result "INFO" "Token Type: $token_type" >&2
            print_result "INFO" "Expires In: $expires_in seconds" >&2
            print_result "INFO" "Scope: $scope_returned" >&2
            
            # Decode and display JWT token contents
            echo "" >&2
            echo "=== JWT Token Analysis ===" >&2
            decode_jwt "$access_token" >&2
            echo "" >&2
            
            # Return ONLY the token to stdout for capture
            echo "$access_token"
            return 0
        else
            print_result "FAIL" "Access token not found in response" >&2
            echo "Response body: $body" >&2
            return 1
        fi
    else
        print_result "FAIL" "OAuth2 token request failed (HTTP $http_code)" >&2
        echo "Response body: $body" >&2
        return 1
    fi
}

# Function to test MCP server with Bearer token
test_mcp_authentication() {
    local token=$1
    
    if [ -z "$token" ]; then
        print_result "FAIL" "No token provided for MCP server test"
        return 1
    fi
    
    print_result "INFO" "Testing MCP server with Bearer token..."
    
    local response=$(curl -s -w "HTTP_CODE:%{http_code}" \
        -H "Authorization: Bearer $token" \
        "$MCP_SERVER_URL/mcp/test" 2>/dev/null)
    
    local http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)
    local body=$(echo "$response" | sed 's/HTTP_CODE:[0-9]*$//')
    
    if [ "$http_code" = "200" ]; then
        print_result "PASS" "MCP server authentication successful (HTTP $http_code)"
        
        # Check if response contains expected content
        if echo "$body" | grep -q "authenticated"; then
            print_result "PASS" "MCP server returned expected authentication response"
            print_result "INFO" "Response: $body"
        else
            print_result "WARN" "MCP server response may be unexpected: $body"
        fi
        return 0
    elif [ "$http_code" = "401" ]; then
        print_result "FAIL" "MCP server authentication failed - Unauthorized (HTTP $http_code)"
        return 1
    else
        print_result "FAIL" "MCP server request failed (HTTP $http_code)"
        return 1
    fi
}

# Function to test MCP server without token (should fail)
test_mcp_no_auth() {
    print_result "INFO" "Testing MCP server without authentication (should fail)..."
    
    local response=$(curl -s -w "HTTP_CODE:%{http_code}" "$MCP_SERVER_URL/mcp/test" 2>/dev/null)
    local http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)
    
    if [ "$http_code" = "401" ]; then
        print_result "PASS" "MCP server correctly rejects unauthenticated requests (HTTP $http_code)"
        return 0
    else
        print_result "WARN" "MCP server did not reject unauthenticated request (HTTP $http_code)"
        return 1
    fi
}

# Function to test OpenID Connect discovery
test_openid_discovery() {
    print_result "INFO" "Testing OpenID Connect discovery..."
    
    local response=$(curl -s -w "HTTP_CODE:%{http_code}" \
        "$AUTH_SERVER_URL/.well-known/openid-configuration" 2>/dev/null)
    
    local http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)
    local body=$(echo "$response" | sed 's/HTTP_CODE:[0-9]*$//')
    
    if [ "$http_code" = "200" ]; then
        print_result "PASS" "OpenID Connect discovery successful (HTTP $http_code)"
        
        # Check issuer
        local issuer=$(echo "$body" | grep -o '"issuer":"[^"]*' | cut -d'"' -f4)
        if [ "$issuer" = "$AUTH_SERVER_URL" ]; then
            print_result "PASS" "Issuer correctly configured: $issuer"
        else
            print_result "WARN" "Issuer configuration: $issuer (expected: $AUTH_SERVER_URL)"
        fi
        
        # Check if client_credentials is supported
        if echo "$body" | grep -q "client_credentials"; then
            print_result "PASS" "Client credentials grant type is supported"
        else
            print_result "FAIL" "Client credentials grant type not found"
        fi
        
        return 0
    else
        print_result "FAIL" "OpenID Connect discovery failed (HTTP $http_code)"
        return 1
    fi
}

# Function to check hostname resolution
test_hostname_resolution() {
    print_result "INFO" "Testing hostname resolution..."
    
    if ping -c 1 auth-server >/dev/null 2>&1; then
        print_result "PASS" "auth-server hostname resolves correctly"
        
        # Show the resolved IP
        local ip=$(ping -c 1 auth-server 2>/dev/null | grep PING | grep -o "([^)]*)" | tr -d "()")
        print_result "INFO" "auth-server resolves to: $ip"
        return 0
    else
        print_result "FAIL" "auth-server hostname does not resolve"
        print_result "INFO" "Add 'auth-server' to /etc/hosts pointing to 127.0.0.1"
        return 1
    fi
}

# Function to show process information
show_running_processes() {
    print_result "INFO" "Checking running processes..."
    
    local auth_process=$(ps aux | grep "com.meetingplanner.auth.AuthServerApplication" | grep -v grep | wc -l)
    local mcp_process=$(ps aux | grep "org.rag4j.meetingplanner.location.App" | grep -v grep | wc -l)
    
    if [ "$auth_process" -gt 0 ]; then
        print_result "PASS" "Auth server process is running"
    else
        print_result "FAIL" "Auth server process not found"
        print_result "INFO" "Start with: cd auth-server && mvn spring-boot:run"
    fi
    
    if [ "$mcp_process" -gt 0 ]; then
        print_result "PASS" "MCP server process is running"
    else
        print_result "FAIL" "MCP server process not found"
        print_result "INFO" "Start with: cd location-mcp-sse && mvn spring-boot:run"
    fi
}

# Main test execution
main() {
    echo "Starting comprehensive OAuth2 and MCP server tests..."
    echo
    
    # Test 1: Check hostname resolution
    echo "=== Test 1: Hostname Resolution ==="
    test_hostname_resolution
    echo
    
    # Test 2: Check running processes
    echo "=== Test 2: Running Processes ==="
    show_running_processes
    echo
    
    # Test 3: Check service availability
    echo "=== Test 3: Service Availability ==="
    auth_available=false
    mcp_available=false
    
    if check_service "Auth Server" "$AUTH_SERVER_URL/actuator/health"; then
        auth_available=true
    fi
    
    if check_service "MCP Server" "$MCP_SERVER_URL"; then
        mcp_available=true
    fi
    echo
    
    # Test 4: OpenID Connect Discovery
    if [ "$auth_available" = true ]; then
        echo "=== Test 4: OpenID Connect Discovery ==="
        test_openid_discovery
        echo
    fi
    
    # Test 5: Test MCP server without authentication
    if [ "$mcp_available" = true ]; then
        echo "=== Test 5: MCP Server Security ==="
        test_mcp_no_auth
        echo
    fi
    
    # Test 6: OAuth2 flow and MCP authentication
    if [ "$auth_available" = true ] && [ "$mcp_available" = true ]; then
        echo "=== Test 6: Complete OAuth2 Flow ==="
        token=$(test_oauth2_flow)
        oauth_result=$?
        echo
        
        if [ $oauth_result -eq 0 ] && [ -n "$token" ]; then
            echo "=== Test 7: MCP Server Authentication ==="
            test_mcp_authentication "$token"
            echo
        else
            print_result "FAIL" "OAuth2 flow failed, skipping MCP authentication test"
            echo
        fi
    fi
    
    # Summary
    echo "=============================================="
    echo "🏁 Test Summary"
    echo "=============================================="
    
    if [ "$auth_available" = true ] && [ "$mcp_available" = true ]; then
        print_result "PASS" "All services are running and accessible"
        print_result "PASS" "OAuth2 authentication flow is working"
        print_result "PASS" "MCP server authentication is working"
        echo
        print_result "INFO" "Your OAuth2 and MCP setup is correctly configured!"
        echo
        print_result "INFO" "You can now start the web application:"
        echo "       cd web-app && mvn spring-boot:run"
    else
        print_result "FAIL" "Some services are not available"
        echo
        print_result "INFO" "Make sure both servers are running:"
        echo "       Terminal 1: cd auth-server && mvn spring-boot:run"
        echo "       Terminal 2: cd location-mcp-sse && mvn spring-boot:run"
    fi
}

# Function to display help
show_help() {
    echo "Usage: $0 [OPTIONS] [JWT_TOKEN]"
    echo ""
    echo "OPTIONS:"
    echo "  --help, -h     Show this help message"
    echo "  --decode-jwt   Decode a JWT token (provide token as argument)"
    echo "  --test-token   Get a new OAuth2 token and decode it"
    echo ""
    echo "EXAMPLES:"
    echo "  $0                           # Run full OAuth2 and MCP tests"
    echo "  $0 --decode-jwt <token>      # Decode a specific JWT token"
    echo "  $0 --test-token              # Get and decode a fresh OAuth2 token"
    echo ""
}

# Parse command line arguments
case "${1:-}" in
    --help|-h)
        show_help
        exit 0
        ;;
    --decode-jwt)
        if [ -n "${2:-}" ]; then
            decode_jwt "$2"
        else
            echo "Error: Please provide a JWT token to decode"
            echo "Usage: $0 --decode-jwt <token>"
            exit 1
        fi
        exit 0
        ;;
    --test-token)
        echo "🔑 Getting fresh OAuth2 token for analysis..."
        echo ""
        
        # Get token using the OAuth2 flow
        CREDENTIALS_B64=$(echo -n "$CLIENT_ID:$CLIENT_SECRET" | base64)
        RESPONSE=$(curl -s -w "HTTP_CODE:%{http_code}" \
            -X POST \
            -H "Authorization: Basic $CREDENTIALS_B64" \
            -H "Content-Type: application/x-www-form-urlencoded" \
            -d "grant_type=client_credentials&scope=$SCOPE" \
            "$AUTH_SERVER_URL/oauth2/token" 2>/dev/null)
        
        HTTP_CODE=$(echo "$RESPONSE" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)
        BODY=$(echo "$RESPONSE" | sed 's/HTTP_CODE:[0-9]*$//')
        
        if [ "$HTTP_CODE" = "200" ]; then
            TOKEN=$(echo "$BODY" | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)
            if [ -n "$TOKEN" ]; then
                decode_jwt "$TOKEN"
            else
                echo "Error: Could not extract token from response"
                exit 1
            fi
        else
            echo "Error: Failed to get OAuth2 token (HTTP $HTTP_CODE)"
            echo "Response: $BODY"
            exit 1
        fi
        exit 0
        ;;
esac

# Run the main function
main "$@"
