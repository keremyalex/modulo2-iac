# Globomatics con AWS CloudFormation

Implementación funcional de Globomatics con stacks anidados, cloud-init y recursos independientes de OpenTofu y Pulumi.

## Arquitectura

```text
Internet
   |
   v
frontend-cf-globomatic :80
   |
   | /api por la VPC CloudFormation
   v
backend-cf-globomatic :8080
   |
   v
PostgreSQL :5432 (localhost)
```

- El frontend usa React y Nginx.
- El backend usa Java 17, Maven, Spring Boot y PostgreSQL 16 nativo.
- PostgreSQL no se expone en AWS.
- El puerto 8080 solo acepta tráfico de `frontend-cf-sg`.
- La contraseña se obtiene del SecureString `/globomatics/cf/database/password`.
- La VPC `10.70.0.0/16` es exclusiva de CloudFormation.

## Estructura

| Archivo | Responsabilidad |
|---|---|
| `template.yaml` | Stack raíz y conexión de outputs/parameters |
| `templates/network.yaml` | VPC, subred, rutas y Security Groups |
| `templates/backend.yaml` | IAM, EC2 backend y cloud-init |
| `templates/frontend.yaml` | EC2 frontend y cloud-init |
| `git-sync-deployment.yaml` | Parámetros para CloudFormation Git Sync |
| `setup/oidc-role.yaml` | Bucket y rol opcional para GitHub Actions |

## Contraseña PostgreSQL

CloudFormation no permite crear directamente un `AWS::SSM::Parameter` de tipo `SecureString`. Por eso el parámetro se crea una vez antes del stack:

```powershell
$dbPassword = Read-Host -MaskInput "Contraseña PostgreSQL CloudFormation"

aws ssm put-parameter `
  --name "/globomatics/cf/database/password" `
  --type "SecureString" `
  --value $dbPassword `
  --overwrite `
  --region "sa-east-1"

Remove-Variable dbPassword
```

Puede usarse la misma contraseña de los otros despliegues, aunque cada implementación tiene su propia base de datos.

## Despliegue local con AWS CLI

El usuario que ejecuta el despliegue necesita permisos de CloudFormation, como mínimo para `ValidateTemplate`, stacks, change sets, descripciones y eventos. También necesita los permisos EC2/IAM/SSM/S3 correspondientes a los recursos del template. Si aparece `AccessDenied` para `cloudformation:ValidateTemplate`, ese permiso aún debe agregarse al usuario.

Se puede reutilizar el bucket existente para subir las plantillas anidadas:

```powershell
cd CloudFormation

aws cloudformation package `
  --template-file "template.yaml" `
  --s3-bucket "globomatic-tfstate-441214868241-sa-east-1" `
  --s3-prefix "cloudformation/globomatics-cf" `
  --output-template-file "packaged.yaml" `
  --region "sa-east-1"
```

Desplegar:

```powershell
aws cloudformation deploy `
  --stack-name "globomatics-cf" `
  --template-file "packaged.yaml" `
  --parameter-overrides `
    KeyName="pk-lab2-ecommerce" `
    SSHLocation="TU_IP_PUBLICA/32" `
    BackendInstanceType="t3.micro" `
    FrontendInstanceType="t3.micro" `
    RepositoryBranch="practico07" `
  --capabilities "CAPABILITY_NAMED_IAM" `
  --region "sa-east-1"
```

## Outputs y prueba

```powershell
aws cloudformation describe-stacks `
  --stack-name "globomatics-cf" `
  --region "sa-east-1" `
  --query "Stacks[0].Outputs" `
  --output table
```

Obtener la URL:

```powershell
$url = aws cloudformation describe-stacks `
  --stack-name "globomatics-cf" `
  --region "sa-east-1" `
  --query "Stacks[0].Outputs[?OutputKey=='ApplicationUrl'].OutputValue" `
  --output text

curl.exe -i --max-time 20 "$url/api/admin/modulos"
```

CloudFormation puede terminar antes que Maven y npm. Si la aplicación todavía no responde, espera unos minutos y revisa `/var/log/cloud-init-output.log` por SSH.

## Git Sync

Los stacks anidados necesitan URLs S3. Por eso Git Sync debe vigilar el archivo generado `CloudFormation/packaged.yaml`, no el `template.yaml` con rutas locales.

Flujo:

1. Ejecutar `aws cloudformation package`.
2. Confirmar `CloudFormation/packaged.yaml` en Git.
3. Subirlo junto con `CloudFormation/git-sync-deployment.yaml`.
4. En CloudFormation elegir **Create stack → Sync from Git**.
5. Conectar el repositorio `keremyalex/modulo2-iac` y la rama `practico07`.
6. Seleccionar **I am providing my own file**.
7. Usar `CloudFormation/git-sync-deployment.yaml` como deployment file.
8. Permitir que AWS cree el rol de Git Sync o seleccionar uno con permisos suficientes. Para estos stacks anidados, el rol también debe poder leer los templates del bucket S3.

Cuando cambie un template anidado se debe volver a ejecutar `package` y confirmar el nuevo `packaged.yaml`; ese cambio es el que Git Sync detecta.

## GitHub Actions opcional

El workflow `.github/workflows/deploy-cloudformation.yml` ofrece una alternativa a Git Sync. Empaqueta y despliega usando OIDC. El bootstrap `setup/oidc-role.yaml` crea el bucket, proveedor y rol necesarios.

Git Sync y GitHub Actions son mecanismos alternativos: para evitar dos despliegues simultáneos sobre el mismo stack, usa uno solo durante la demostración.

## Eliminación

```powershell
aws cloudformation delete-stack `
  --stack-name "globomatics-cf" `
  --region "sa-east-1"

aws cloudformation wait stack-delete-complete `
  --stack-name "globomatics-cf" `
  --region "sa-east-1"
```

La red y las EC2 se eliminan con el stack. El SecureString fue creado externamente y se elimina aparte:

```powershell
aws ssm delete-parameter `
  --name "/globomatics/cf/database/password" `
  --region "sa-east-1"
```
