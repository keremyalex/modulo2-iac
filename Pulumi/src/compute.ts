import * as aws from "@pulumi/aws";
import { renderCloudInit } from "./cloudInit";
import { Settings } from "./config";
import { BackendIdentity } from "./iam";
import { Network } from "./network";

export interface CreateInstancesArgs {
    settings: Settings;
    network: Network;
    databaseSecret: aws.ssm.Parameter;
    backendIdentity: BackendIdentity;
}

export interface ApplicationInstances {
    backend: aws.ec2.Instance;
    frontend: aws.ec2.Instance;
}

export function createInstances(args: CreateInstancesArgs): ApplicationInstances {
    const { settings, network, databaseSecret, backendIdentity } = args;

    const ami = aws.ec2.getAmiOutput({
        mostRecent: true,
        owners: ["amazon"],
        filters: [
            { name: "name", values: ["al2023-ami-2023.*-x86_64"] },
            { name: "virtualization-type", values: ["hvm"] },
            { name: "architecture", values: ["x86_64"] },
            { name: "state", values: ["available"] },
        ],
    });

    const backendUserData = databaseSecret.name.apply((parameterName) =>
        renderCloudInit("backend.yaml.tmpl", {
            REPOSITORY_URL: settings.repositoryUrl,
            REPOSITORY_BRANCH: settings.repositoryBranch,
            DB_PARAMETER_NAME: parameterName,
            AWS_REGION: settings.region,
        }),
    );

    const backend = new aws.ec2.Instance(
        "backend-pm-globomatic",
        {
            ami: ami.id,
            instanceType: settings.backendInstanceType,
            keyName: settings.keyName,
            associatePublicIpAddress: true,
            vpcSecurityGroupIds: [network.backendSecurityGroup.id],
            iamInstanceProfile: backendIdentity.instanceProfile.name,
            userData: backendUserData,
            userDataReplaceOnChange: true,
            metadataOptions: { httpTokens: "required" },
            tags: { ...settings.commonTags, Name: "backend-pm-globomatic" },
        },
        { dependsOn: [backendIdentity.rolePolicy] },
    );

    const frontendUserData = backend.privateIp.apply((backendPrivateIp) =>
        renderCloudInit("frontend.yaml.tmpl", {
            REPOSITORY_URL: settings.repositoryUrl,
            REPOSITORY_BRANCH: settings.repositoryBranch,
            BACKEND_PRIVATE_IP: backendPrivateIp,
        }),
    );

    const frontend = new aws.ec2.Instance("frontend-pm-globomatic", {
        ami: ami.id,
        instanceType: settings.frontendInstanceType,
        keyName: settings.keyName,
        associatePublicIpAddress: true,
        vpcSecurityGroupIds: [network.frontendSecurityGroup.id],
        userData: frontendUserData,
        userDataReplaceOnChange: true,
        metadataOptions: { httpTokens: "required" },
        tags: { ...settings.commonTags, Name: "frontend-pm-globomatic" },
    });

    return { backend, frontend };
}
