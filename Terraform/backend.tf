terraform {
  # Configuracion del backend remoto
  backend "s3" {
    bucket         = "globomatic-tfstate-estado"        # El bucket que creaste
    key            = "globomatic/env/terraform.tfstate" # Ruta del archivo dentro del bucket
    region         = "sa-east-1"
    encrypt        = true
    dynamodb_table = "terraform-state-lock" # La tabla que acabas de crear
  }
}