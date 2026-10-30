# Práctico 07 — Despliegue con OpenTofu y cloud-init en AWS

Esta guía explica cómo desplegar Globomatics en AWS usando la configuración de la carpeta `Terraform`.

## ¿Qué se crea?

- Una EC2 `t3.micro` llamada `frontend-tf-globomatic`, con React y Nginx.
- Una EC2 `t3.micro` llamada `backend-tf-globomatic`, con Java 17 y Spring Boot.
- PostgreSQL 16 instalado directamente en la EC2 backend, sin Docker.
- Un parámetro `SecureString` en AWS Systems Manager Parameter Store para la contraseña de PostgreSQL.
- Security Groups para HTTP, SSH y comunicación privada entre frontend y backend.
- Estado remoto de OpenTofu en S3 y bloqueo del estado mediante DynamoDB.

El flujo de comunicación es:

```text
Usuario -> Frontend EC2 :80 -> Nginx -> Backend EC2 :8080 -> PostgreSQL :5432
```

El puerto 8080 solamente permite tráfico proveniente del Security Group del frontend. PostgreSQL escucha únicamente en `localhost` dentro del backend.

## Requisitos previos

Antes de desplegar se necesita:

1. Una cuenta de AWS y AWS CLI configurado.
2. OpenTofu instalado.
3. Un key pair llamado `pk-lab2-ecommerce` en la región `sa-east-1`.
4. El bucket S3 `globomatic-tfstate-441214868241-sa-east-1`.
5. La tabla DynamoDB `terraform-state-lock`, con partition key `LockID` de tipo String.
6. La rama `practico07` disponible en el repositorio de GitHub.

El repositorio configurado actualmente es:

```text
https://github.com/keremyalex/modulo2-iac.git
```

El repositorio debe ser público para que las EC2 puedan clonarlo sin credenciales. Si se utiliza un repositorio privado, se debe implementar un mecanismo seguro de autenticación.

## 1. Comprobar la conexión con AWS

```powershell
aws sts get-caller-identity
aws configure get region
```

La región utilizada por el proyecto es `sa-east-1`.

## 2. Entrar a la carpeta de Terraform

```powershell
cd Terraform
```

Todos los comandos de OpenTofu de esta guía se ejecutan desde esa carpeta.

## 3. Proporcionar la contraseña de PostgreSQL

La contraseña no tiene un valor predeterminado y no debe escribirse en los archivos ni subirse a GitHub. En PowerShell se puede introducir de forma oculta con:

```powershell
$env:TF_VAR_db_password = Read-Host -MaskInput "Contraseña de PostgreSQL"
```

Se debe escribir la misma contraseña cuando sea necesario conservar el acceso a la base de datos existente.

La variable solo dura durante la sesión actual de PowerShell. Si se cierra la terminal o se abre una nueva, debe configurarse otra vez.

## 4. Inicializar OpenTofu

```powershell
tofu init
```

Este comando descarga los proveedores y conecta el proyecto con el backend remoto configurado en S3 y DynamoDB.

Se debe ejecutar nuevamente `tofu init` cuando cambie la configuración de `backend.tf`.

## 5. Validar la configuración

```powershell
tofu fmt -check
tofu validate
```

## 6. Crear y revisar el plan

```powershell
tofu plan -out="globomatics.tfplan"
```

El carácter de `-out` debe ser un guion normal `-`, no un guion largo `—`.

Antes de continuar, se debe revisar cuántos recursos se crearán, modificarán o eliminarán.

## 7. Aplicar el plan

```powershell
tofu apply "globomatics.tfplan"
```

OpenTofu crea la infraestructura y AWS ejecuta un cloud-init diferente en cada EC2:

- `cloud-init/backend.yaml.tftpl`: instala PostgreSQL, Java y Maven; crea la base de datos; compila el backend y levanta el servicio.
- `cloud-init/frontend.yaml.tftpl`: instala Node.js y Nginx; compila el frontend y configura el proxy hacia el backend.

Aunque `tofu apply` termine, cloud-init puede necesitar algunos minutos adicionales para descargar e instalar dependencias.

## 8. Obtener la URL de la aplicación

```powershell
tofu output -raw application_url
```
También se pueden consultar todas las salidas con:

```powershell
tofu output
```

La aplicación se abre utilizando la URL pública del frontend. El backend se consume mediante `/api`; no se accede directamente por su IP pública y el puerto 8080.

Ejemplo para comprobar el backend a través de Nginx:

```powershell
$url = tofu output -raw application_url
curl.exe -i --max-time 20 "$url/api/admin/modulos"
```

## 9. Diagnóstico

Para comprobar el estado general de las instancias:

```powershell
aws ec2 describe-instance-status --include-all-instances --region sa-east-1
```

En la EC2 backend:

```bash
cloud-init status --long
sudo tail -n 200 /var/log/cloud-init-output.log
sudo systemctl status postgresql --no-pager
sudo systemctl status globomatics-backend --no-pager
sudo journalctl -u globomatics-backend -n 100 --no-pager
```

En la EC2 frontend:

```bash
cloud-init status --long
sudo tail -n 200 /var/log/cloud-init-output.log
sudo systemctl status nginx --no-pager
sudo nginx -t
```

Si el frontend funciona pero las solicitudes `/api` quedan esperando hasta producir timeout, se debe revisar que exista la regla `frontend-sg -> backend-sg` para el puerto TCP 8080. Un nuevo `tofu plan` detecta normalmente esta diferencia entre el estado y AWS.

## 10. Aplicar cambios posteriormente

En una terminal nueva:

```powershell
cd Terraform
$env:TF_VAR_db_password = Read-Host -MaskInput "Contraseña de PostgreSQL"
tofu plan -out="globomatics.tfplan"
tofu apply "globomatics.tfplan"
```

Cambiar una plantilla cloud-init provoca el reemplazo de la EC2 correspondiente porque las instancias tienen `user_data_replace_on_change = true`.

Los datos de PostgreSQL se guardan actualmente en el disco raíz del backend. Si la instancia backend se destruye o reemplaza, esos datos también se pierden.

## 11. Destruir la infraestructura

La forma recomendada es crear y revisar primero un plan de destrucción:

```powershell
$env:TF_VAR_db_password = Read-Host -MaskInput "Contraseña de PostgreSQL"
tofu plan -destroy -out="destroy.tfplan"
tofu apply "destroy.tfplan"
```

También se puede ejecutar directamente:

```powershell
tofu destroy
```

Esto elimina los recursos administrados por este proyecto, incluidas las EC2, los Security Groups, el rol IAM y el parámetro SSM. No elimina el bucket S3 ni la tabla DynamoDB, porque fueron creados previamente y no están declarados como recursos en esta configuración.

Para volver a crear la infraestructura después de un `destroy`, se repiten los pasos de contraseña, plan y apply:

```powershell
$env:TF_VAR_db_password = Read-Host -MaskInput "Contraseña de PostgreSQL"
tofu plan -out="globomatics.tfplan"
tofu apply "globomatics.tfplan"
```

## Comandos resumidos

```powershell
cd Terraform
$env:TF_VAR_db_password = Read-Host -MaskInput "Contraseña de PostgreSQL"
tofu init
tofu fmt -check
tofu validate
tofu plan -out="globomatics.tfplan"
tofu apply "globomatics.tfplan"
tofu output -raw application_url
```
