import * as pulumi from "@pulumi/pulumi";
import { loadSettings } from "./src/config";
import { createNetwork } from "./src/network";
import { createDatabaseSecret } from "./src/secrets";
import { createBackendIdentity } from "./src/iam";
import { createInstances } from "./src/compute";

const settings = loadSettings();
const network = createNetwork(settings);
const databaseSecret = createDatabaseSecret(settings);
const backendIdentity = createBackendIdentity(settings, databaseSecret.arn);

const { backend, frontend } = createInstances({
    settings,
    network,
    databaseSecret,
    backendIdentity,
});

export const backendInstanceId = backend.id;
export const backendPublicIp = backend.publicIp;
export const backendPrivateIp = backend.privateIp;
export const frontendInstanceId = frontend.id;
export const frontendPublicIp = frontend.publicIp;
export const applicationUrl = pulumi.interpolate`http://${frontend.publicIp}`;
