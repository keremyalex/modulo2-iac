import * as pulumi from "@pulumi/pulumi";

export interface Settings {
    region: string;
    keyName: string;
    sshCidr: string;
    repositoryUrl: string;
    repositoryBranch: string;
    backendInstanceType: string;
    frontendInstanceType: string;
    dbPassword: pulumi.Output<string>;
    commonTags: Record<string, string>;
}

export function loadSettings(): Settings {
    const config = new pulumi.Config();
    const awsConfig = new pulumi.Config("aws");

    const dbPassword = config.requireSecret("dbPassword").apply((password) => {
        if (!/^[A-Za-z0-9_@%+=:,.!-]{12,128}$/.test(password)) {
            throw new Error("dbPassword debe tener entre 12 y 128 caracteres y solo letras, numeros o _@%+=:,.!-");
        }
        return password;
    });

    return {
        region: awsConfig.get("region") ?? "sa-east-1",
        keyName: config.get("keyName") ?? "pk-lab2-ecommerce",
        sshCidr: config.get("sshCidr") ?? "168.228.132.62/32",
        repositoryUrl: config.get("repositoryUrl") ?? "https://github.com/keremyalex/modulo2-iac.git",
        repositoryBranch: config.get("repositoryBranch") ?? "practico07",
        backendInstanceType: config.get("backendInstanceType") ?? "t3.micro",
        frontendInstanceType: config.get("frontendInstanceType") ?? "t3.micro",
        dbPassword,
        commonTags: {
            Project: "globomatic",
            ManagedBy: "Pulumi",
            Variant: "pm",
        },
    };
}
