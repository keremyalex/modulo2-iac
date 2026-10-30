import * as fs from "node:fs";
import * as path from "node:path";

export function renderCloudInit(fileName: string, values: Record<string, string>): string {
    const templatePath = path.join(process.cwd(), "cloud-init", fileName);
    let rendered = fs.readFileSync(templatePath, "utf8");

    for (const [key, value] of Object.entries(values)) {
        rendered = rendered.split(`@@${key}@@`).join(value);
    }

    const missingTokens = rendered.match(/@@[A-Z0-9_]+@@/g);
    if (missingTokens) {
        throw new Error(`Faltan valores para la plantilla ${fileName}: ${missingTokens.join(", ")}`);
    }

    return rendered;
}
