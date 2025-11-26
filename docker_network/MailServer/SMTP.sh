#!/bin/bash

SERVER="localhost"
PORT="999"
SENDER="testuser@uliege.be"
RECIPIENT="dcd@uliege.be"
RECIPIENT1="rom@uliege.be"
RECIPIENT2="dcd@gembloux.uliege.be"

echo "Attempting to connect to $SERVER on port $PORT..."

{
    sleep 0.5
    echo "HELO uliege.be"
    sleep 0.5

    echo "MAIL FROM:$SENDER"
    sleep 0.5

    echo "RCPT TO:$RECIPIENT"
    sleep 0.5
    echo "RCPT TO:$RECIPIENT1"
    sleep 0.5
    echo "RCPT TO:$RECIPIENT2"
    sleep 0.5

    echo "DATA"
    sleep 0.5

    echo "Subject: Telnet Script Test"
    echo "From: $SENDER"
    echo "To: $RECIPIENT"
    echo ""
    echo "Hello, this message was sent manually using a Telnet script."
    echo "End of message."
    echo "."
    sleep 0.5

    echo "QUIT"
} | telnet "$SERVER" "$PORT"

echo "--- Script finished. Check server logs for delivery status. ---"
