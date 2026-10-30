# Práctico 07 — CloudFormation con stacks anidados y Git Sync

Esta implementación crea una tercera copia independiente de Globomatics en AWS usando CloudFormation.

## Recursos principales

- `frontend-cf-globomatic`: React y Nginx.
- `backend-cf-globomatic`: Spring Boot y PostgreSQL 16 nativo.
- `frontend-cf-sg` y `backend-cf-sg`.
- VPC `globomatics-cf-vpc` con CIDR `10.70.0.0/16`.
- Rol `globomatics-cf-backend-role`.
- SecureString externo `/globomatics/cf/database/password`.

Los recursos `cf` no pertenecen a OpenTofu ni Pulumi y pueden funcionar simultáneamente con ellos.

## Archivos

```text
CloudFormation/
├── template.yaml
├── packaged.yaml                  # generado por package
├── git-sync-deployment.yaml
├── templates/
│   ├── network.yaml
│   ├── backend.yaml
│   └── frontend.yaml
└── setup/
    └── oidc-role.yaml
```

`template.yaml` orquesta los tres stacks anidados. `backend.yaml` y `frontend.yaml` contienen sus respectivos cloud-init.

## 1. Crear la contraseña

CloudFormation no soporta crear directamente un parámetro SSM `SecureString`, por lo que se prepara antes del stack:

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

La contraseña debe tener entre 12 y 128 caracteres. Puede utilizarse, por ejemplo, la misma contraseña de OpenTofu, aunque las bases de datos son independientes.

## 2. Empaquetar los stacks anidados

El usuario IAM debe tener los permisos de CloudFormation para validar templates y administrar stacks/change sets, además de los permisos EC2, IAM, SSM y S3 usados por esta infraestructura.

Desde la raíz del repositorio:

```powershell
cd CloudFormation

aws cloudformation package `
  --template-file "template.yaml" `
  --s3-bucket "globomatic-tfstate-441214868241-sa-east-1" `
  --s3-prefix "cloudformation/globomatics-cf" `
  --output-template-file "packaged.yaml" `
  --region "sa-east-1"
```

El comando sube `network.yaml`, `backend.yaml` y `frontend.yaml` a S3 y genera `packaged.yaml` con sus URLs.

## 3. Desplegar

Reemplaza `TU_IP_PUBLICA` con la IP autorizada para SSH:

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

## 4. Consultar el progreso

```powershell
aws cloudformation describe-stacks `
  --stack-name "globomatics-cf" `
  --region "sa-east-1" `
  --query "Stacks[0].StackStatus" `
  --output text
```

Eventos recientes:

```powershell
aws cloudformation describe-stack-events `
  --stack-name "globomatics-cf" `
  --region "sa-east-1" `
  --query "StackEvents[0:15].[Timestamp,ResourceStatus,LogicalResourceId,ResourceStatusReason]" `
  --output table
```

## 5. Obtener la URL

```powershell
$url = aws cloudformation describe-stacks `
  --stack-name "globomatics-cf" `
  --region "sa-east-1" `
  --query "Stacks[0].Outputs[?OutputKey=='ApplicationUrl'].OutputValue" `
  --output text

$url
```

Cloud-init puede necesitar varios minutos adicionales. Después prueba:

```powershell
curl.exe -i --max-time 20 "$url/api/admin/modulos"
```

## 6. Configurar Git Sync

Git Sync no convierte las rutas locales de los stacks anidados en URLs S3. Por eso se utiliza el `packaged.yaml` generado anteriormente.

Antes de configurarlo:

1. Confirma `CloudFormation/packaged.yaml` en Git.
2. Confirma también `CloudFormation/git-sync-deployment.yaml`.
3. Sube ambos a la rama `practico07` del repositorio personal.

En AWS:

1. Abre CloudFormation en `sa-east-1`.
2. Selecciona **Create stack → Sync from Git**.
3. Conecta GitHub mediante CodeConnections.
4. Elige `keremyalex/modulo2-iac` y la rama `practico07`.
5. Selecciona que ya proporcionarás el deployment file.
6. Indica `CloudFormation/git-sync-deployment.yaml`.
7. Deja que AWS cree el rol de Git Sync o selecciona uno apropiado. Debe tener acceso de lectura a los templates anidados almacenados en S3.
8. Revisa y crea el stack.

Cuando cambie cualquier archivo dentro de `templates/`, vuelve a ejecutar `aws cloudformation package` y confirma el nuevo `packaged.yaml`.

## 7. Diagnóstico

En el backend:

```bash
cloud-init status --long
sudo tail -n 200 /var/log/cloud-init-output.log
sudo systemctl status postgresql --no-pager
sudo systemctl status globomatics-backend --no-pager
sudo journalctl -u globomatics-backend -n 100 --no-pager
```

En el frontend:

```bash
cloud-init status --long
sudo tail -n 200 /var/log/cloud-init-output.log
sudo systemctl status nginx --no-pager
sudo nginx -t
```

## 8. Eliminar

```powershell
aws cloudformation delete-stack `
  --stack-name "globomatics-cf" `
  --region "sa-east-1"

aws cloudformation wait stack-delete-complete `
  --stack-name "globomatics-cf" `
  --region "sa-east-1"
```

Después, si ya no se utilizará la contraseña:

```powershell
aws ssm delete-parameter `
  --name "/globomatics/cf/database/password" `
  --region "sa-east-1"
```

La eliminación del stack CloudFormation no afecta las instancias `tf` ni `pm`.
