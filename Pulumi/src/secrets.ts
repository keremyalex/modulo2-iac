import * as aws from "@pulumi/aws";
import { Settings } from "./config";

export function createDatabaseSecret(settings: Settings): aws.ssm.Parameter {
    return new aws.ssm.Parameter("globomatics-pm-database-password", {
        name: "/globomatics/pm/database/password",
        description: "Contrasena PostgreSQL del backend Pulumi",
        type: "SecureString",
        value: settings.dbPassword,
        tags: { ...settings.commonTags, Name: "globomatics-pm-database-password" },
    });
}
