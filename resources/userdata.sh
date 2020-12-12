#!/bin/bash
exec > >(tee /var/log/user-data.log|logger -t user-data -s 2>/dev/console) 2>&1

set -e
set -x
cd /home/ec2-user
yum update -y
yum install -y docker
service docker start
usermod -a -G docker ec2-user
curl -L https://github.com/docker/compose/releases/download/${DOCKER_COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose
chmod a+x /usr/local/bin/docker-compose
yum install libtool -y
wget https://dl.google.com/go/go${GO_VERSION}.linux-amd64.tar.gz
tar -xzf go${GO_VERSION}.linux-amd64.tar.gz
mv go /usr/local
yum install libtool-ltdl-devel -y
pip install --upgrade awscli
yum install git -y
echo 'export GOROOT=/usr/local/go
              export GOPATH=/home/ec2-user/go
              export PATH=$GOROOT/bin:$PATH' >>/home/ec2-user/.bash_profile
source /home/ec2-user/.bash_profile
wget https://github.com/hyperledger/fabric-ca/releases/download/v${FABRIC_CA_VERSION}/hyperledger-fabric-ca-linux-amd64-${FABRIC_CA_VERSION}.tar.gz
tar -xzf hyperledger-fabric-ca-linux-amd64-${FABRIC_CA_VERSION}.tar.gz
cd /home/ec2-user
echo 'export PATH=$PATH:/home/ec2-user/bin' >>/home/ec2-user/.bash_profile
echo 'export MSP_PATH=/opt/home/admin-msp
              export MSP=${MEMBER_ID}
              export ORDERER=${ORDERING_SERVICE_ENDPOINT}
              export PEER=${PEER_NODE_ENDPOINT}
              export CA_ENDPOINT=${FABRIC_CA_ENDPOINT}' >>/home/ec2-user/.bash_profile
source /home/ec2-user/.bash_profile
# Setup Fabric-ca client profile
mkdir -p /home/ec2-user/.fabric-ca-client
touch /home/ec2-user/.fabric-ca-client/fabric-ca-client-config.yaml
echo '
              #############################################################################
              # Client Configuration
              #############################################################################
              # URL of the Fabric-ca-server (default: http://localhost:7054)
                                url: https://${FABRIC_CA_ENDPOINT}
              # Membership Service Provider (MSP) directory
              # This is useful when the client is used to enroll a peer or orderer, so
              # that the enrollment artifacts are stored in the format expected by MSP.
                                mspdir: /home/ec2-user/admin-msp
              #############################################################################
              #    TLS section for secure socket connection
              #
              #  certfiles - PEM-encoded list of trusted root certificate files
              #############################################################################
                                tls:
                                  # TLS section for secure socket connection
                                  certfiles: /home/ec2-user/managedblockchain-tls-chain.pem
              ' >/home/ec2-user/.fabric-ca-client/fabric-ca-client-config.yaml
chmod 666 /home/ec2-user/.fabric-ca-client/fabric-ca-client-config.yaml
# Download TLS cert
wget ${TLS_CERT_URL}
# Download sample chaincode from github
git clone -b ${FABRIC_SAMPLES_BRANCH} https://github.com/hyperledger/fabric-samples.git
# Bake in some fabric related ENV variables for convenience
echo 'export ORDERER=${ORDERING_SERVICE_ENDPOINT}' >>/home/ec2-user/.bash_profile
echo 'version: '"'2'"'
services:
  cli:
    container_name: cli
    image: hyperledger/fabric-tools:${FABRIC_TOOLS_VERSION}
    tty: true
    environment:
      - GOPATH=/opt/gopath
      - CORE_VM_ENDPOINT=unix:///host/var/run/docker.sock
      - CORE_LOGGING_LEVEL=info # Set logging level to debug for more verbose logging
      - CORE_PEER_ID=cli
      - CORE_CHAINCODE_KEEPALIVE=10
      - CORE_PEER_LOCALMSPID=${MEMBER_ID}
      - CORE_PEER_MSPCONFIGPATH=/opt/home/admin-msp
      - CORE_PEER_ADDRESS=${PEER_NODE_ENDPOINT}
      - CORE_PEER_TLS_ROOTCERT_FILE=/opt/home/managedblockchain-tls-chain.pem
      - CORE_PEER_TLS_ENABLED=true
    working_dir: /opt/home
    command: /bin/bash
    volumes:
      - /var/run/:/host/var/run/
      - /home/ec2-user/fabric-samples/chaincode:/opt/gopath/src/github.com/
      - /home/ec2-user:/opt/home' >docker-compose-cli.yaml
newgrp $(id -gn)
/usr/local/bin/docker-compose -f docker-compose-cli.yaml up -d