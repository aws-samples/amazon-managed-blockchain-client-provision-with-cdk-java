/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: MIT-0
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.myorg;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.ec2.AmazonLinuxImage;
import software.amazon.awscdk.services.ec2.ISecurityGroup;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.Instance;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.UserData;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;
import software.amazon.awscdk.services.iam.IRole;
import software.amazon.awscdk.services.iam.Role;

public class AmbNetworkClientStack extends Stack {
        public AmbNetworkClientStack(final Construct scope, final String id) {
                this(scope, id, null);
        }

        public AmbNetworkClientStack(final Construct scope, final String id, final StackProps props) {
                super(scope, id, props);

                final String vpcId = "<VPD ID>";
                final String securityGroupId = "<SECURITY GROUP ID>";
                final String instanceType = "<EC2 INSTANCE TYPE>"; // e.g. t3.small
                final String keyPairName = "<KEY PAIR NAME>"; 
                final String instaceRoleARN = "<INSTANCE IAM ROLE ARN>";
                final String frameworkVersion = "<HYPERLEDGER FABRIC VERSION>";//e.g. 1.4
                final String memberID = "<MEMBER ID>";
                final String peerNodeEndpoint = "<PEER END POINT>";
                final String orderingServiceEndpoint = "<ORDERING SERVICE END POINT>";
                final String fabricCAEndpoint = "<CERTIFICATE AUTHORITY(CA) END POINT>";

                IVpc clientVPC = Vpc.fromLookup(this, "AMBClientVPC", VpcLookupOptions.builder().vpcId(vpcId).build());

                ISecurityGroup clientSecurityGroup = SecurityGroup.fromSecurityGroupId(this, "AMBClientSecurityGroup",
                                securityGroupId);

                IRole instaceRole = Role.fromRoleArn(this, "InstanceRole", instaceRoleARN);

                String dockerComposeVersion = "";
                String goVersion = "";
                String fabricToolsVersion = "";
                String fabricCAVersion = "";
                String fabricSamplesBranch = "";
                String tlsCertURL = String.format(
                                "https://s3.%s.amazonaws.com/%s.managedblockchain/etc/managedblockchain-tls-chain.pem",
                                this.getRegion(), this.getRegion());

                if (frameworkVersion.equals("1.2")) {
                        dockerComposeVersion = "1.20.0";
                        goVersion = "1.10.3";
                        fabricToolsVersion = "1.2.1";
                        fabricCAVersion = "1.2.1";
                        fabricSamplesBranch = "release-1.2";
                } else if (frameworkVersion.equals("1.4")) {
                        dockerComposeVersion = "1.20.0";
                        goVersion = "1.14.2";
                        fabricToolsVersion = "1.4.7";
                        fabricCAVersion = "1.4.7";
                        fabricSamplesBranch = "release-1.4";
                }

                Path path = Paths.get(System.getProperty("user.dir") + "/resources/userdata.sh");
                String userDataScript = "";
                try {
                        userDataScript = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
                } catch (IOException e) {
                        e.printStackTrace();
                }
                userDataScript = userDataScript.replace("${DOCKER_COMPOSE_VERSION}", dockerComposeVersion);
                userDataScript = userDataScript.replace("${GO_VERSION}", goVersion);
                userDataScript = userDataScript.replace("${FABRIC_CA_VERSION}", fabricCAVersion);
                userDataScript = userDataScript.replace("${TLS_CERT_URL}", tlsCertURL);
                userDataScript = userDataScript.replace("${FABRIC_TOOLS_VERSION}", fabricToolsVersion);
                userDataScript = userDataScript.replace("${FABRIC_SAMPLES_BRANCH}", fabricSamplesBranch);
                userDataScript = userDataScript.replace("${MEMBER_ID}", memberID);
                userDataScript = userDataScript.replace("${PEER_NODE_ENDPOINT}", peerNodeEndpoint);
                userDataScript = userDataScript.replace("${ORDERING_SERVICE_ENDPOINT}", orderingServiceEndpoint);
                userDataScript = userDataScript.replace("${FABRIC_CA_ENDPOINT}", fabricCAEndpoint);

                UserData userData = UserData.custom(userDataScript);

                Instance clientInstance = Instance.Builder.create(this, "AMBClientInstance")
                                .instanceType(new InstanceType(instanceType)).machineImage(new AmazonLinuxImage())
                                .userData(userData).vpc(clientVPC).keyName(keyPairName).role(instaceRole)
                                .vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PUBLIC).build())
                                .securityGroup(clientSecurityGroup).build();

        }
}
