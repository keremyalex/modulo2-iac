# EC2 con Pulumi y TypeScript

Este proyecto crea una instancia `t3.micro` con Amazon Linux 2023 en `us-east-2`, según `Pulumi.dev.yaml`. Usa la VPC y el grupo de seguridad predeterminados de esa región. No configura acceso SSH ni abre puertos adicionales.

## Requisitos

- Pulumi CLI, Node.js y npm.
- Credenciales de AWS configuradas con `aws configure` o un perfil equivalente.
- Una VPC predeterminada en la región seleccionada.

## Ejecutar

```powershell
npm install
pulumi stack select dev
pulumi preview
pulumi up
pulumi stack output instanceId
pulumi stack output publicIp
```

`pulumi up` crea recursos facturables en AWS. Revisa el plan antes de aprobarlo. Si la cuenta no tiene VPC predeterminada, configura una subred y un grupo de seguridad explícitos en `index.ts`.

Para borrar la instancia cuando termines:

```powershell
pulumi destroy
```

## Si falla el plugin de Node.js en Windows

El mensaje `nodejs plugin ... did not begin responding to RPC connections` ocurre al iniciar el runtime de Pulumi, antes de ejecutar `index.ts`. Comprueba que el binario y Node.js funcionen:

```powershell
pulumi version
node --version
& 'C:\Program Files (x86)\Pulumi\pulumi-language-nodejs.exe' -version
```

El CLI y el plugin se comunican mediante `127.0.0.1`. Ejecuta `pulumi preview --stack dev` en una PowerShell normal, fuera de una terminal aislada. Si usas un proxy, excluye `127.0.0.1` y `localhost` de él. Si persiste el error, ejecuta `pulumi preview --stack dev --logtostderr -v=5` y revisa si el firewall o antivirus bloquea conexiones locales. Reinstalar paquetes npm no soluciona un bloqueo de ese puerto.
