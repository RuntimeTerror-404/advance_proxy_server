# test-rate-limit.sh
# this scripts is responsible fr testing rate limiting functionality of proxy server
for i in {1..15}
do
    echo "Request $i"
    curl -X GET "http://localhost:8080/proxy?url=https://example.com" -H "User-ID: user123"
    echo "\n"
done
