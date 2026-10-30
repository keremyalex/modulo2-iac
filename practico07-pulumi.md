# Práctico 07 — Despliegue con Pulumi y cloud-init en AWS

Esta guía permite desplegar Globomatics con Pulumi y TypeScript sin interferir con las instancias administradas por OpenTofu.

## Estructura del código

```text
Pulumi/
├── index.ts
├── src/
│   ├── config.ts
│   ├── network.ts
│   ├── secrets.ts
│   ├── iam.ts
│   ├── cloudInit.ts
│   └── compute.ts
└── cloud-init/
    ├── backend.yaml.tmpl
    └── frontend.yaml.tmpl
```

`index.ts` conecta los módulos y publica los outputs. La configuración, red, secretos, permisos, renderizado de cloud-init e instancias están separados por responsabilidad dentro de `src/`.

## Recursos creados

- EC2 `frontend-pm-globomatic`: React y Nginx.
- EC2 `backend-pm-globomatic`: Java 17, Spring Boot y PostgreSQL 16 nativo.
- Security Groups `frontend-pm-sg` y `backend-pm-sg`.
- Rol y perfil IAM exclusivos del backend Pulumi.
- Parámetro SSM `/globomatics/pm/database/password`.
- Regla privada desde el frontend al backend por TCP 8080.

```text
Usuario -> frontend-pm-globomatic:80
                    |
                    | /api por red privada
                    v
           backend-pm-globomatic:8080
                    |
                    v
           PostgreSQL:5432 (localhost)
```

## Requisitos previos

1. AWS CLI configurado.
2. Pulumi CLI instalado.
3. Node.js y npm instalados.
4. Key pair `pk-lab2-ecommerce` en `sa-east-1`.
5. Bucket `globomatic-tfstate-441214868241-sa-east-1`.
6. Rama `practico07` disponible en el repositorio configurado.

Comprobar AWS:

```powershell
aws sts get-caller-identity
aws configure get region
```

## 1. Entrar al proyecto e instalar dependencias

Desde la raíz del repositorio:

```powershell
cd Pulumi
npm install
```

Validar TypeScript:

```powershell
npm run typecheck
```

## 2. Crear o seleccionar el stack

Para la primera ejecución:

```powershell
pulumi stack init dev
```

Para ejecuciones posteriores:

```powershell
pulumi stack select dev
```

El proyecto almacena su estado en una ruta propia dentro del bucket S3. No comparte el archivo de estado de OpenTofu y no utiliza la tabla DynamoDB de OpenTofu.

Pulumi puede solicitar una frase de contraseña para cifrar los secretos del stack. Esta frase debe conservarse: se necesitará posteriormente para ejecutar `preview`, `up` o `destroy`.

## 3. Guardar la contraseña de PostgreSQL

```powershell
pulumi config set --secret dbPassword
```

Pulumi solicitará el valor sin guardarlo como texto plano en `Pulumi.dev.yaml`.
La contraseña debe tener entre 12 y 128 caracteres y utilizar solamente letras, números o `_@%+=:,.!-`.

## 4. Revisar la configuración

```powershell
pulumi config
```

Valores predeterminados principales:

| Configuración | Valor |
|---|---|
| Región | `sa-east-1` |
| Key pair | `pk-lab2-ecommerce` |
| Repositorio | `https://github.com/keremyalex/modulo2-iac.git` |
| Rama | `practico07` |
| Backend | `t3.micro` |
| Frontend | `t3.micro` |

Un valor se puede modificar, por ejemplo:

```powershell
pulumi config set sshCidr "TU_IP_PUBLICA/32"
```

## 5. Previsualizar los cambios

```powershell
pulumi preview
```

Se debe revisar la cantidad de recursos que Pulumi propone crear, actualizar o eliminar.

## 6. Crear la infraestructura

```powershell
pulumi up
```

Después de confirmar, Pulumi crea los recursos. Las EC2 ejecutan plantillas diferentes:

- `cloud-init/backend.yaml.tmpl`: PostgreSQL, base de datos, Java, Maven, compilación del JAR y servicio systemd.
- `cloud-init/frontend.yaml.tmpl`: Node.js, compilación de React, Nginx y proxy `/api`.

Cloud-init puede seguir trabajando durante algunos minutos después de que `pulumi up` finalice.

## 7. Obtener y probar la aplicación

```powershell
pulumi stack output applicationUrl
```

Probar el backend mediante Nginx:

```powershell
$url = pulumi stack output applicationUrl
curl.exe -i --max-time 20 "$url/api/admin/modulos"
```

El puerto 8080 del backend no está abierto al equipo local. Solo acepta conexiones provenientes del Security Group del frontend Pulumi.

## 8. Consultar todos los outputs

```powershell
pulumi stack output
```

Se muestran:

- `applicationUrl`
- `backendInstanceId`
- `backendPrivateIp`
- `backendPublicIp`
- `frontendInstanceId`
- `frontendPublicIp`

## 9. Diagnóstico por SSH

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

## 10. Aplicar cambios

Después de modificar TypeScript, cloud-init o la configuración:

```powershell
npm run typecheck
pulumi preview
pulumi up
```

Modificar un cloud-init reemplaza la EC2 correspondiente porque se utiliza `userDataReplaceOnChange`.

## 11. Destruir la infraestructura Pulumi

```powershell
pulumi preview --destroy
pulumi destroy
```

Esto elimina solamente los recursos administrados por el stack Pulumi. No elimina las instancias OpenTofu, el bucket S3 ni la tabla DynamoDB.

Los datos PostgreSQL del backend Pulumi se encuentran en su volumen raíz y se pierden cuando la instancia se destruye o reemplaza.

## Comandos resumidos

```powershell
cd Pulumi
npm install
npm run typecheck
pulumi stack init dev
pulumi config set --secret dbPassword
pulumi preview
pulumi up
pulumi stack output applicationUrl
```
