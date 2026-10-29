# Terraform/OpenTofu — Globomatics en AWS

Infraestructura como código para crear dos instancias EC2 con Amazon Linux 2023 y configurarlas automáticamente mediante cloud-init.

## Arquitectura

- `backend-globomatic` (`t3.micro` por defecto): PostgreSQL 16 nativo, Java 17, Maven y Spring Boot en el puerto 8080.
- `frontend-globomatic` (`t3.micro` por defecto): Node.js 22 para compilar React y Nginx para servirlo por HTTP.
- Nginx envía las solicitudes `/api` a la IP privada del backend.
- PostgreSQL solo escucha en `localhost`; el puerto 5432 no se expone en AWS.
- La contraseña de PostgreSQL se guarda como `SecureString` en Systems Manager Parameter Store.
- El estado de OpenTofu se almacena en S3 y se bloquea mediante DynamoDB.

```text
Internet
   |
   v
Frontend EC2 :80 (Nginx + React)
   |
   | /api por la red privada
   v
Backend EC2 :8080 (Spring Boot)
   |
   v
PostgreSQL :5432 (localhost)
```

## Estructura

| Archivo | Contenido |
|---|---|
| `compute.tf` | EC2 y asignación de las plantillas cloud-init |
| `cloud-init/backend.yaml.tftpl` | Instalación y configuración del backend y PostgreSQL |
| `cloud-init/frontend.yaml.tftpl` | Compilación de React y configuración de Nginx |
| `iam.tf` | Instance Role que permite al backend leer un único parámetro SSM |
| `secrets.tf` | Contraseña de base de datos como parámetro `SecureString` |
| `security_groups.tf` | SSH, HTTP/HTTPS y acceso frontend → backend:8080 |
| `data.tf` | Última AMI de Amazon Linux 2023 |
| `backend.tf` | Estado remoto S3 y lock en DynamoDB |
| `variables.tf` | Región, tipos de instancia, repositorio y contraseña |
| `outputs.tf` | IDs, IPs y URL pública de la aplicación |

## Requisitos

- OpenTofu 1.5 o superior.
- Credenciales AWS configuradas mediante AWS CLI, variables de entorno o un perfil.
- Key pair `pk-lab2-ecommerce` en `sa-east-1`, o cambiar `key_name`.
- Bucket `globomatic-tfstate-estado` con versionado y cifrado habilitados.
- Tabla DynamoDB `terraform-state-lock` con partition key `LockID` de tipo String.
- La rama indicada por `repository_branch` debe existir en GitHub y el repositorio debe ser accesible por HTTPS.

La rama por defecto es `practico07`. Antes del primer despliegue, publícala:

```powershell
git push -u origin practico07
```

## Despliegue

Desde `Terraform/`, proporciona una contraseña de al menos 12 caracteres usando solo letras, números o `_@%+=:,.!-`:

```powershell
$env:TF_VAR_db_password = "Cambia-Esta-Clave-2026"
tofu init
tofu plan
tofu apply
```

`tofu apply` crea ambas EC2. AWS ejecuta un cloud-init distinto en cada una durante su primer arranque. El comando puede finalizar antes de que Maven y npm terminen.

Obtén la URL con:

```powershell
tofu output -raw application_url
```

## Comprobar cloud-init

En cada instancia:

```bash
cloud-init status --wait
sudo tail -n 200 /var/log/cloud-init-output.log
```

En el backend:

```bash
sudo systemctl status postgresql
sudo systemctl status globomatics-backend
sudo journalctl -u globomatics-backend -n 100 --no-pager
```

En el frontend:

```bash
sudo systemctl status nginx
sudo nginx -t
```

## Variables principales

| Variable | Default | Descripción |
|---|---|---|
| `region_server` | `sa-east-1` | Región AWS |
| `key_name` | `pk-lab2-ecommerce` | Key pair para SSH |
| `ssh_cidr` | `168.228.132.62/32` | Red autorizada para SSH |
| `backend_instance_type` | `t3.micro` | Tamaño de la EC2 backend |
| `frontend_instance_type` | `t3.micro` | Tamaño de la EC2 frontend |
| `repository_url` | Repositorio Globomatics | Repositorio clonado por cloud-init |
| `repository_branch` | `practico07` | Rama desplegada |
| `db_password` | Sin default | Contraseña sensible de PostgreSQL |

El backend crea 1 GiB de swap para reducir fallos de memoria durante la compilación en `t3.micro`. Si el servicio queda sin memoria durante el uso normal, cambia solo el backend:

```powershell
tofu apply -var="backend_instance_type=t3.small"
```

## Actualizaciones y limpieza

`user_data_replace_on_change = true` hace que cambiar una plantilla cloud-init reemplace la EC2 correspondiente. Cloud-init está pensado para el aprovisionamiento inicial; no sustituye un pipeline de despliegue continuo.

```powershell
tofu destroy
```

Al destruir o reemplazar el backend se elimina también PostgreSQL y sus datos porque se encuentran en el volumen raíz. Para información persistente de producción se recomienda un volumen EBS independiente o Amazon RDS.

## Seguridad

- No guardes `TF_VAR_db_password` en Git.
- El valor sensible existe en el estado remoto; limita el acceso al bucket y mantenlo cifrado.
- El backend obtiene el secreto mediante un Instance Role limitado a `/globomatics/database/password`.
- El puerto 8080 solo acepta tráfico del Security Group del frontend.
- El puerto 5432 no se abre.
- IMDSv2 es obligatorio en ambas instancias.
