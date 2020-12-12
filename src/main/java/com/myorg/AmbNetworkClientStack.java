package com.myorg;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.cxapi.VpcSubnet;
import software.amazon.awscdk.services.ec2.AmazonLinuxImage;
import software.amazon.awscdk.services.ec2.ExecuteFileOptions;
import software.amazon.awscdk.services.ec2.IPublicSubnet;
import software.amazon.awscdk.services.ec2.ISecurityGroup;
import software.amazon.awscdk.services.ec2.ISubnet;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.Instance;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.InterfaceVpcEndpoint;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.PublicSubnet;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SelectedSubnets;
import software.amazon.awscdk.services.ec2.Subnet;
import software.amazon.awscdk.services.ec2.SubnetConfiguration;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.UserData;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcAttributes;
import software.amazon.awscdk.services.ec2.VpcEndpointService;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;
import software.amazon.awscdk.services.iam.IRole;
import software.amazon.awscdk.services.iam.Role;

public class AmbNetworkClientStack extends Stack {
        public AmbNetworkClientStack(final Construct scope, final String id) {
                this(scope, id, null);
        }

        public AmbNetworkClientStack(final Construct scope, final String id, final StackProps props) {
                super(scope, id, props);
                
                final String vpcId = "vpc-6c8fd116";
                final String securityGroupId = "sg-a7512bf2";
                final String instanceType = "t3.small";
                final String keyPairName = "amb-client";
                final String instaceRoleARN = "arn:aws:iam::036842120113:role/ServiceRoleForHyperledgerFabricClient";
                final String frameworkVersion = "1.4";
                final String memberID = "m-KGBFM73CABATRKJZT7XN7S4UJY";
                final String peerNodeEndpoint = "nd-w7fqnwo2ajgklghrjhwz3j2ome.m-kgbfm73cabatrkjzt7xn7s4ujy.n-yz36mjouonbwnjqbao5m7gu624.managedblockchain.us-east-1.amazonaws.com:30003"; 
                final String orderingServiceEndpoint = "orderer.n-yz36mjouonbwnjqbao5m7gu624.managedblockchain.us-east-1.amazonaws.com:30001"; 
                final String fabricCAEndpoint = "ca.m-kgbfm73cabatrkjzt7xn7s4ujy.n-yz36mjouonbwnjqbao5m7gu624.managedblockchain.us-east-1.amazonaws.com:30002";

                IVpc clientVPC = Vpc.fromLookup(this, "AMBClientVPC", VpcLookupOptions.builder().vpcId(vpcId).build());

                ISecurityGroup clientSecurityGroup = SecurityGroup.fromSecurityGroupId(this, "AMBClientSecurityGroup",
                                securityGroupId);

                IRole instaceRole = Role.fromRoleArn(this, "InstanceRole", instaceRoleARN);

                String dockerComposeVersion = ""; 
                String goVersion = "";
                String fabricToolsVersion = "";
                String fabricCAVersion = "";
                String fabricSamplesBranch = "";
                String tlsCertURL = String.format("https://s3.%s.amazonaws.com/%s.managedblockchain/etc/managedblockchain-tls-chain.pem", this.getRegion(), this.getRegion());

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


                String userDataArguments = String.format("%s %s %s %s %s %s %s %s %s %s", dockerComposeVersion,
                                goVersion, fabricCAVersion, tlsCertURL, fabricToolsVersion, fabricSamplesBranch,
                                memberID, peerNodeEndpoint, orderingServiceEndpoint, fabricCAEndpoint);
                UserData userData = UserData.forLinux();
                userData.addExecuteFileCommand(ExecuteFileOptions.builder().filePath("/resources/userdata.sh")
                                .arguments(userDataArguments).build());

                Instance clientInstance = Instance.Builder.create(this, "AMBClientInstance")
                                .instanceType(new InstanceType(instanceType)).machineImage(new AmazonLinuxImage())
                                .userData(userData).vpc(clientVPC).keyName(keyPairName).role(instaceRole)
                                .vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PUBLIC).build())
                                .securityGroup(clientSecurityGroup).build();

        }
}
