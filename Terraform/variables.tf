variable "region_server" {
  description = "Region donde se crearan las instancias EC2"
  type        = string
  default     = "sa-east-1"
}

variable "key_name" {
  description = "Nombre del key pair existente en AWS"
  type        = string
  default     = "pk-lab2-ecommerce"
}

variable "ssh_cidr" {
  description = "CIDR desde el cual se permite SSH de administracion al frontend y backend"
  type        = string
  default     = "168.228.132.62/32"
}

variable "backend_instance_type" {
  description = "Tipo de instancia EC2 para el backend"
  type        = string
  default     = "t3.micro"

  validation {
    condition     = contains(["t3.micro", "t3.small", "t3.medium"], var.backend_instance_type)
    error_message = "El backend debe usar t3.micro, t3.small o t3.medium."
  }
}

variable "frontend_instance_type" {
  description = "Tipo de instancia EC2 para el frontend"
  type        = string
  default     = "t3.micro"

  validation {
    condition     = contains(["t3.micro", "t3.small", "t3.medium"], var.frontend_instance_type)
    error_message = "El frontend debe usar t3.micro, t3.small o t3.medium."
  }
}

variable "repository_url" {
  description = "URL HTTPS del repositorio que cloud-init clonara"
  type        = string
  default     = "https://github.com/keremyalex/modulo2-iac.git"

  validation {
    condition     = can(regex("^https://", var.repository_url))
    error_message = "repository_url debe ser una URL HTTPS."
  }
}

variable "repository_branch" {
  description = "Rama del repositorio que se desplegara en las instancias"
  type        = string
  default     = "practico07"

  validation {
    condition     = can(regex("^[A-Za-z0-9._/-]+$", var.repository_branch))
    error_message = "repository_branch contiene caracteres no validos."
  }
}

variable "db_password" {
  description = "Contrasena del usuario PostgreSQL globomatics_app"
  type        = string
  sensitive   = true

  validation {
    condition     = can(regex("^[A-Za-z0-9_@%+=:,.!-]{12,128}$", var.db_password))
    error_message = "db_password debe tener entre 12 y 128 caracteres y solo letras, numeros o _@%+=:,.!-"
  }
}
