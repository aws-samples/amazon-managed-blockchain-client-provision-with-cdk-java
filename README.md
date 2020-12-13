# Provision Amazon Managed Blockchain Client EC2 Instance using CDK and Java Programming Language

## Introduction

This Sample provides Java code to provision Amazon Managed Blockchain Client EC2 Instance using AWS Cloud Development Kit(CDK).

Know more details about AWS CDK here https://aws.amazon.com/cdk/

Know more details about Amazon Managed Blockchain here https://aws.amazon.com/managed-blockchain

## Prerequisites

* Java 8 (v1.8) or later
* Apache Maven 3.5 or later
* CDK 1.69 
* AWS Command Line Interface V2

## How to use this Sample

Before using code from this sample make sure following are provisioned/created.

* **Amazon Managed Blockchain Network** You may refer this [Sample](https://github.com/aws-samples/amazon-managed-blockchain-network-provision-with-cdk-java) to provision using CDK 
* **VPC**
* **Public Subnet**
* **Security Group**
* **Rules to allow Inbound and Outbound traffic between members of Security Group**
* **Interface VPC Endpoint to connect to Amazon Managed Blockchain** You may refer this [URL](https://docs.aws.amazon.com/managed-blockchain/latest/managementguide/get-started-create-endpoint.html)
* **IAM role for Amazon EC2 instance** You may refer this [URL](https://docs.aws.amazon.com/managed-blockchain/latest/managementguide/security_iam_hyperledger_ec2_client.html)
* **SSH key pair for Amazon EC2 instance**

Provide below values in AmbNetworkClientApp.java file.

* **`<ACCOUNT ID>`**
* **`<REGION>`**

Provide below values in AmbNetworkClientStack.java file.

* **`<VPD ID>`**
* **`<SECURITY GROUP ID>`**
* **`<EC2 INSTANCE TYPE>`**
* **`<KEY PAIR NAME>`**
* **`<INSTANCE IAM ROLE ARN>`**
* **`<HYPERLEDGER FABRIC VERSION>`**
* **`<MEMBER ID>`**
* **`<PEER END POINT>`**
* **`<ORDERING SERVICE END POINT>`**
* **`<CERTIFICATE AUTHORITY(CA) END POINT>`**

After providing above values, execute the following commands in terminal from the root directory.

* `cdk synth` this command generates the CloudFormation template under cdk.out folder. Validate the template.
* `cdk deploy` this command deploys CloudFormation stack

## Security

See CONTRIBUTING file for more information.

## License

This library is licensed under the MIT-0 License. See the LICENSE file.
