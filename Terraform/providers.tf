provider "aws" {
  region     = var.region_server
  access_key = var.access_key
  secret_key = var.secret_access_key

  default_tags {
    tags = {
      Project   = "globomatic"
      ManagedBy = "opentofu"
    }
  }
}
