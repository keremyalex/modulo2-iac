# Globomatics con Pulumi

Infraestructura equivalente al despliegue OpenTofu, implementada con Pulumi y TypeScript.

## Arquitectura

- `backend-pm-globomatic`: EC2 `t3.micro` con PostgreSQL 16 nativo, Java 17, Maven y Spring Boot.
- `frontend-pm-globomatic`: EC2 `t3.micro` con Node.js 22, React y Nginx.
- Nginx envía `/api` al backend por su IP privada y el puerto 8080.
- PostgreSQL solo escucha en `localhost` y no expone el puerto 5432.
- La contraseña se guarda como `SecureString` en `/globomatics/pm/database/password`.
- El estado de Pulumi utiliza el bucket S3 existente bajo una ruta independiente.

```text
Internet
   |
   v
Frontend PM :80
   |
   | /api
   v
Backend PM :8080
   |
   v
PostgreSQL :5432 (localhost)
```

## Archivos

| Archivo | Función |
|---|---|
| `index.ts` | Orquestación de los módulos y outputs |
| `src/config.ts` | Lectura y validación de la configuración |
| `src/network.ts` | Security Groups y acceso frontend → backend:8080 |
| `src/secrets.ts` | Contraseña PostgreSQL en Parameter Store |
| `src/iam.ts` | Rol, política y perfil IAM del backend |
| `src/cloudInit.ts` | Lectura y renderizado de plantillas cloud-init |
| `src/compute.ts` | AMI e instancias EC2 backend y frontend |
| `cloud-init/backend.yaml.tmpl` | PostgreSQL y backend Spring Boot |
| `cloud-init/frontend.yaml.tmpl` | React, Nginx y proxy `/api` |
| `Pulumi.yaml` | Proyecto, backend S3 y valores predeterminados |
| `package.json` | Dependencias y validación TypeScript |

## Requisitos

- Pulumi CLI, Node.js y npm.
- AWS CLI configurado con permisos sobre EC2, IAM, SSM y el bucket de estado.
- Key pair `pk-lab2-ecommerce` en `sa-east-1`.
- Bucket `globomatic-tfstate-441214868241-sa-east-1`.
- Repositorio y rama configurados accesibles por HTTPS.

## Configurar y desplegar

```powershell
cd Pulumi
npm install
pulumi stack init dev
pulumi config set --secret dbPassword
pulumi preview
pulumi up
```

La contraseña debe tener entre 12 y 128 caracteres y utilizar solamente letras, números o `_@%+=:,.!-`.

Al utilizar el backend S3, Pulumi puede solicitar una frase de contraseña para cifrar los secretos del stack. Debe conservarse para poder administrar o destruir el stack posteriormente.

Si el stack ya existe:

```powershell
pulumi stack select dev
```

Consultar la URL:

```powershell
pulumi stack output applicationUrl
```

Comprobar el API mediante el frontend:

```powershell
$url = pulumi stack output applicationUrl
curl.exe -i --max-time 20 "$url/api/admin/modulos"
```

## Configuración opcional

```powershell
pulumi config set keyName "pk-lab2-ecommerce"
pulumi config set sshCidr "168.228.132.62/32"
pulumi config set repositoryUrl "https://github.com/keremyalex/modulo2-iac.git"
pulumi config set repositoryBranch "practico07"
pulumi config set backendInstanceType "t3.micro"
pulumi config set frontendInstanceType "t3.micro"
```

Después de cambiar una configuración:

```powershell
pulumi preview
pulumi up
```

## Validar

```powershell
npm run typecheck
pulumi preview
```

## Diagnóstico

Backend:

```bash
cloud-init status --long
sudo tail -n 200 /var/log/cloud-init-output.log
sudo systemctl status postgresql --no-pager
sudo systemctl status globomatics-backend --no-pager
sudo journalctl -u globomatics-backend -n 100 --no-pager
```

Frontend:

```bash
cloud-init status --long
sudo tail -n 200 /var/log/cloud-init-output.log
sudo systemctl status nginx --no-pager
sudo nginx -t
```

## Destruir

```powershell
pulumi preview --destroy
pulumi destroy
```

La destrucción elimina los recursos del stack Pulumi, pero no elimina el bucket S3 compartido. Los datos de PostgreSQL están en el volumen raíz y se pierden al destruir o reemplazar el backend.
