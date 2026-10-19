import * as aws from "@pulumi/aws";

// Imagen oficial de Amazon Linux 2023 para instancias x86_64.
const ami = aws.ec2.getAmiOutput({
    mostRecent: true,
    owners: ["amazon"],
    filters: [
        { name: "name", values: ["al2023-ami-2023.*-x86_64"] },
        { name: "virtualization-type", values: ["hvm"] },
        { name: "root-device-type", values: ["ebs"] },
    ],
});

// Usa la VPC predeterminada de la región configurada en Pulumi.
const server = new aws.ec2.Instance("mi-ec2", {
    ami: ami.id,
    instanceType: aws.ec2.InstanceType.T3_Micro,
    metadataOptions: { httpTokens: "required" },
    tags: { Name: "mi-ec2-pulumi" },
});

export const instanceId = server.id;
export const publicIp = server.publicIp;
