import * as aws from "@pulumi/aws";
import * as pulumi from "@pulumi/pulumi";
import { Settings } from "./config";

export interface BackendIdentity {
    role: aws.iam.Role;
    rolePolicy: aws.iam.RolePolicy;
    instanceProfile: aws.iam.InstanceProfile;
}

export function createBackendIdentity(
    settings: Settings,
    parameterArn: pulumi.Input<string>,
): BackendIdentity {
    const assumeRole = aws.iam.getPolicyDocumentOutput({
        statements: [
            {
                effect: "Allow",
                actions: ["sts:AssumeRole"],
                principals: [{ type: "Service", identifiers: ["ec2.amazonaws.com"] }],
            },
        ],
    });

    const role = new aws.iam.Role("globomatics-pm-backend-role", {
        name: "globomatics-pm-backend-role",
        assumeRolePolicy: assumeRole.json,
        tags: { ...settings.commonTags, Name: "globomatics-pm-backend-role" },
    });

    const parameterAccess = aws.iam.getPolicyDocumentOutput({
        statements: [
            {
                effect: "Allow",
                actions: ["ssm:GetParameter"],
                resources: [parameterArn],
            },
        ],
    });

    const rolePolicy = new aws.iam.RolePolicy("globomatics-pm-read-database-password", {
        name: "globomatics-pm-read-database-password",
        role: role.id,
        policy: parameterAccess.json,
    });

    const instanceProfile = new aws.iam.InstanceProfile("globomatics-pm-backend-profile", {
        name: "globomatics-pm-backend-profile",
        role: role.name,
        tags: { ...settings.commonTags, Name: "globomatics-pm-backend-profile" },
    });

    return { role, rolePolicy, instanceProfile };
}
