# Terraform — Infraestructura de Backend y Frontend (AWS)

Infraestructura como código (IaC) con OpenTofu para levantar las instancias EC2 del backend y el frontend del Sistema de Control y Registro de Asistencia Estudiantil en AWS.

**Stack:** OpenTofu ≥ 1.5 · Provider AWS `~> 6.0` · Estado remoto en S3 + lock en DynamoDB

---

## Arquitectura

- **2 instancias EC2** `t3.micro` con Amazon Linux 2023 en la región `sa-east-1`:
  - `backend-globomatic` — API del sistema
  - `frontend-globomatic` — interfaz web
- **Security Groups:**
  - `backend-sg`: SSH (puerto 22) restringido a `var.ssh_cidr`; tráfico de salida abierto
  - `frontend-sg`: SSH restringido a `var.ssh_cidr`, HTTP (80) y HTTPS (443) públicos
- **Regla** `allow_frontend_to_backend`: permite al frontend llamar al backend en el puerto 8080
- **IMDSv2 obligatorio** en ambas instancias (`http_tokens = "required"`)
- **Estado remoto** en S3 (`globomatic-tfstate-estado`) con bloqueo de concurrentes en DynamoDB (`terraform-state-lock`)

## Estructura de archivos

| Archivo | Contenido |
|---|---|
| `versions.tf` | Versión mínima de OpenTofu y proveedor AWS requerido |
| `providers.tf` | Configuración del provider AWS y `default_tags` (`Project=globomatic`, `ManagedBy=opentofu`) |
| `data.tf` | Data source para buscar la última AMI de Amazon Linux 2023 |
| `security_groups.tf` | Security groups de backend y frontend + regla 8080 |
| `compute.tf` | Instancias EC2 de backend y frontend |
| `backend.tf` | Backend remoto S3 + DynamoDB para el estado |
| `variables.tf` | Definición de variables |
| `outputs.tf` | Outputs de las instancias |
| `env.tf` | Credenciales AWS (ignorado por git, no se sube) |

## Requisitos

- [OpenTofu](https://opentofu.org/) 1.5 o superior instalado
- Key pair `pk-lab2-ecommerce` creado en la región `sa-east-1` (o cambiar `key_name` en `variables.tf`)
- Bucket S3 `globomatic-tfstate-estado` y tabla DynamoDB `terraform-state-lock` ya creados en `sa-east-1`
- Credenciales de un usuario IAM con permisos sobre ese bucket y tabla

## Configuración de credenciales

El backend remoto y el provider toman las credenciales de la cadena estándar de AWS. Exportalas en tu shell:

```bash
export AWS_ACCESS_KEY_ID=<tu-access-key>
export AWS_SECRET_ACCESS_KEY=<tu-secret-key>
export AWS_DEFAULT_REGION=sa-east-1
```

> **Nota:** `env.tf` está en `.gitignore` — nunca se sube a GitHub. Si prefieres, puedes usar un perfil de `~/.aws/credentials` en lugar de las variables de entorno.

## Uso

```bash
# 1. Inicializar (descarga providers y conecta el backend S3)
tofu init

# 2. Revisar el plan de cambios
tofu plan

# 3. Aplicar la infraestructura
tofu apply

# 4. (opcional) Destruir todo al terminar el laboratorio
tofu destroy
```

Los outputs (IP pública, IP privada e ID de las instancias) se muestran al final del `apply` y también con:

```bash
tofu output
```

## Variables

| Variable | Descripción | Default |
|---|---|---|
| `region_server` | Región de AWS donde se crean los recursos | `sa-east-1` |
| `key_name` | Nombre del key pair existente en AWS para SSH | `pk-lab2-ecommerce` |
| `ssh_cidr` | CIDR desde el cual se permite SSH de administración | `168.228.132.62/32` |

## Outputs

| Output | Descripción |
|---|---|
| `svr_backend_globomatic_id` | ID de la instancia EC2 Backend |
| `svr_backend_globomatic_public_ip` | IP pública de la instancia Backend |
| `svr_backend_globomatic_private_ip` | IP privada de la instancia Backend |
| `svr_frontend_globomatic_id` | ID de la instancia EC2 Frontend |
| `svr_frontend_globomatic_public_ip` | IP pública de la instancia Frontend |

## Seguridad

- El SSH está restringido a una única IP (`var.ssh_cidr`); cambia tu IP desde `variables.tf` o con `-var="ssh_cidr=x.x.x.x/32"`
- El frontend solo expone HTTP/HTTPS públicamente; el backend solo es accesible desde el frontend (8080) y por SSH
- IMDSv2 obligatorio: las instancias no pueden leer metadatos sin token
- Todos los recursos se etiquetan automáticamente vía `default_tags` del provider
- Las credenciales nunca se versionan (`env.tf` ignorado, secretos por variables de entorno)
