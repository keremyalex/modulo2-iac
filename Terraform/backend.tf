terraform {
  # Configuracion del backend remoto
  backend "s3" {
    bucket         = "globomatic-tfstate-441214868241-sa-east-1" # Bucket de estado de esta cuenta
    key            = "globomatic/env/terraform.tfstate"          # Ruta del archivo dentro del bucket
    region         = "sa-east-1"
    encrypt        = true
    dynamodb_table = "terraform-state-lock" # La tabla que acabas de crear
  }
}
