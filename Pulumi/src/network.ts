import * as aws from "@pulumi/aws";
import { Settings } from "./config";

export interface Network {
    frontendSecurityGroup: aws.ec2.SecurityGroup;
    backendSecurityGroup: aws.ec2.SecurityGroup;
    apiAccessRule: aws.ec2.SecurityGroupRule;
}

export function createNetwork(settings: Settings): Network {
    const defaultVpc = aws.ec2.getVpcOutput({ default: true });

    const frontendSecurityGroup = new aws.ec2.SecurityGroup("frontend-pm-sg", {
        name: "frontend-pm-sg",
        description: "Security Group del frontend administrado con Pulumi",
        vpcId: defaultVpc.id,
        ingress: [
            {
                description: "SSH de administracion",
                protocol: "tcp",
                fromPort: 22,
                toPort: 22,
                cidrBlocks: [settings.sshCidr],
            },
            {
                description: "HTTP publico",
                protocol: "tcp",
                fromPort: 80,
                toPort: 80,
                cidrBlocks: ["0.0.0.0/0"],
            },
            {
                description: "HTTPS publico",
                protocol: "tcp",
                fromPort: 443,
                toPort: 443,
                cidrBlocks: ["0.0.0.0/0"],
            },
        ],
        egress: [
            {
                description: "Salida para actualizaciones y dependencias",
                protocol: "-1",
                fromPort: 0,
                toPort: 0,
                cidrBlocks: ["0.0.0.0/0"],
            },
        ],
        tags: { ...settings.commonTags, Name: "frontend-pm-sg" },
    });

    const backendSecurityGroup = new aws.ec2.SecurityGroup("backend-pm-sg", {
        name: "backend-pm-sg",
        description: "Security Group del backend administrado con Pulumi",
        vpcId: defaultVpc.id,
        ingress: [
            {
                description: "SSH de administracion",
                protocol: "tcp",
                fromPort: 22,
                toPort: 22,
                cidrBlocks: [settings.sshCidr],
            },
        ],
        egress: [
            {
                description: "Salida para actualizaciones y dependencias",
                protocol: "-1",
                fromPort: 0,
                toPort: 0,
                cidrBlocks: ["0.0.0.0/0"],
            },
        ],
        tags: { ...settings.commonTags, Name: "backend-pm-sg" },
    });

    const apiAccessRule = new aws.ec2.SecurityGroupRule("frontend-pm-to-backend-pm-8080", {
        type: "ingress",
        description: "API desde el frontend Pulumi",
        protocol: "tcp",
        fromPort: 8080,
        toPort: 8080,
        securityGroupId: backendSecurityGroup.id,
        sourceSecurityGroupId: frontendSecurityGroup.id,
    });

    return { frontendSecurityGroup, backendSecurityGroup, apiAccessRule };
}
