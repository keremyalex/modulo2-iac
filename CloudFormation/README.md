# Globomatics — Infraestructura con AWS CloudFormation

Infraestructura como código para desplegar la red y los servidores de **Globomatics** usando **CloudFormation con stacks anidados** (nested stacks).

## Estructura del proyecto

```
CloudFormation/
├── template.yaml          # Raíz: orquesta los 3 stacks anidados
├── templates/
│   ├── network.yaml       # Red: VPC, subred pública, IGW, rutas, Security Group
│   ├── backend.yaml       # Instancia EC2 Backend
│   └── frontend.yaml      # Instancia EC2 Frontend
├── .gitignore             # Ignora *.pem y packaged.yaml
└── README.md
```

## Qué despliega

| Template | Recursos | Descripción |
|---|---|---|
| `template.yaml` | 3 `AWS::CloudFormation::Stack` | Pasa parámetros y conecta los outputs de red con las instancias |
| `templates/network.yaml` | 8 | VPC `10.0.0.0/16`, subred pública `10.0.1.0/24`, Internet Gateway, tabla de rutas con salida `0.0.0.0/0` y Security Group (HTTP 80 abierto, SSH 22 restringido) |
| `templates/backend.yaml` | 1 | EC2 `t3.micro` con Amazon Linux 2023 — `Globomatics-Backend` |
| `templates/frontend.yaml` | 1 | EC2 `t3.micro` con Amazon Linux 2023 — `Globomatics-Frontend` |

Las instancias se conectan a la red mediante outputs de la raíz:

```yaml
SubnetId: !GetAtt NetworkStack.Outputs.PublicSubnetId
SecurityGroupId: !GetAtt NetworkStack.Outputs.WebSecurityGroupId
```

## Parámetros

| Parámetro | Default | Descripción |
|---|---|---|
| `KeyName` | `pk-lab2-ecommerce` | Key Pair existente en AWS para SSH (se usa en ambas instancias) |
| `SSHLocation` | `0.0.0.0/0` | CIDR autorizado para SSH. **Usa siempre tu IP**: `<tu-ip>/32` |
| `InstanceType` | `t3.micro` | Tipo de instancia para Backend y Frontend (`t3.micro`/`t3.small`/`t3.medium`) |
| `VpcCidr` (en `network.yaml`) | `10.0.0.0/16` | Bloque CIDR de la VPC |
| `SubnetCidr` (en `network.yaml`) | `10.0.1.0/24` | Bloque CIDR de la subred pública |

## Outputs

`VpcId`, `BackendPublicIP`, `FrontendPublicIP`, `BackendInstanceId`, `FrontendInstanceId`.

## Despliegue

`TemplateURL` de los stacks anidados apunta a rutas relativas (`./templates/*.yaml`), que **deben subirse a S3**. El comando `aws cloudformation package` los sube automáticamente y genera un template empaquetado.

```bash
cd CloudFormation/

# 1. Empaquetar: sube templates/ a S3 y genera packaged.yaml
aws cloudformation package \
  --template-file template.yaml \
  --s3-bucket <bucket-artefactos> \
  --output-template-file packaged.yaml

# 2. Desplegar
aws cloudformation deploy \
  --stack-name globomatics \
  --template-file packaged.yaml \
  --parameter-overrides \
       KeyName=pk-lab2-ecommerce \
       SSHLocation=<tu-ip>/32 \
       InstanceType=t3.micro
```

Ver el estado:

```bash
aws cloudformation describe-stacks --stack-name globomatics \
  --query 'Stacks[0].Outputs'
```

## Despliegue automático (CI/CD con GitHub Actions)

Al hacer `push` a `main` con cambios en `CloudFormation/**`, el workflow
`.github/workflows/deploy-cloudformation.yml` empaqueta y despliega el stack solo.
Usa **OIDC**: no hay claves de AWS en GitHub, solo un rol IAM de corta duración.

```
push a main (CloudFormation/**)
  → checkout
  → asume rol IAM vía OIDC (token de GitHub)
  → aws cloudformation package  (sube templates/ al bucket de artefactos)
  → aws cloudformation deploy   (crea/actualiza el stack globomatics)
```

### 1. Bootstrap (una sola vez, desde tu CLI con credenciales)

```bash
cd CloudFormation/

aws cloudformation deploy \
  --stack-name globomatics-oidc \
  --template-file setup/oidc-role.yaml \
  --capabilities CAPABILITY_IAM
```

Crea el bucket de artefactos, el proveedor OIDC de GitHub y el rol de despliegue.
Parámetros opcionales: `GitHubRepo`, `GitHubBranch`, `ArtifactBucketName` y
`CreateOidcProvider=false` si el proveedor OIDC ya existe en la cuenta.

Luego copia los outputs:

```bash
aws cloudformation describe-stacks --stack-name globomatics-oidc \
  --query 'Stacks[0].Outputs'
```

### 2. Variables y secretos en GitHub

En **Settings → Secrets and variables → Actions**:

| Tipo | Nombre | Ejemplo |
|---|---|---|
| Secret | `AWS_ROLE_ARN` | output `RoleArn` |
| Secret | `CF_SSH_LOCATION` | `<tu-ip>/32` |
| Variable | `CF_ARTIFACTS_BUCKET` | output `ArtifactBucketName` |
| Variable | `AWS_REGION` | `sa-east-1` |
| Variable | `CF_STACK_NAME` | `globomatics` |
| Variable | `CF_KEY_NAME` | `pk-lab2-ecommerce` |
| Variable | `CF_INSTANCE_TYPE` | `t3.micro` |

### 3. Primer despliegue

Haz commit y push a `main` (o dispara el workflow con **Run workflow** desde la
pestaña Actions). Si el stack no existe lo crea; si no hay cambios termina en
no-op. Revisa el resultado en la pestaña **Actions** del repositorio.

> Si cambias de IP, actualiza `CF_SSH_LOCATION`; si cambias el nombre del bucket
> o el rol, actualiza `CF_ARTIFACTS_BUCKET` / `AWS_ROLE_ARN`.

## Políticas de retención

- **Red (`network.yaml`)**: los 8 recursos tienen `DeletionPolicy: Retain` y `UpdateReplacePolicy: Retain`, por lo que **sobreviven al borrado del stack** (VPC, subred, IGW, rutas y SG no tienen costo fijo).
- **EC2 (`backend.yaml`, `frontend.yaml`)**: sin política → se **eliminan** con el stack para no dejar instancias huérfanas facturando.
- **Stacks raíz**: sin política → se borran en cascada.

> Nota: como la red retenida ya no pertenece a ningún stack, un siguiente `deploy` crea una red **nueva**; la anterior queda como recurso huérfano (sin costo) que puedes eliminar manualmente desde la consola si ya no la necesitas.

## Limpieza

```bash
# Borra instancias y stacks (la red se conserva por la política Retain)
aws cloudformation delete-stack --stack-name globomatics
aws cloudformation wait stack-delete-complete --stack-name globomatics
```
