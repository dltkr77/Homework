#!/bin/bash

# SSH's public key sharing
expect << EOF
    spawn ssh-keygen -t rsa
    expect "Enter file in which to save the key (/home/hadoop//.ssh/id_rsa):"
        send "\n"
    expect "Enter passphrase (empty for no passphrase):"
        send "\n"
    expect "Enter same passphrase again:"
        send "\n"
    expect eof
EOF

cat ~/.ssh/id_rsa.pub > ~/.ssh/authorized_keys
cat ~/.ssh/id_rsa.pub | ssh hadoop@slave1 "mkdir ~/.ssh; cat > ~/.ssh/authorized_keys"
cat ~/.ssh/id_rsa.pub | ssh hadoop@slave2 "mkdir ~/.ssh; cat > ~/.ssh/authorized_keys"
