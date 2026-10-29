provider "aws" {
  region = var.region_server

  default_tags {
    tags = {
      Project   = "globomatic"
      ManagedBy = "opentofu"
    }
  }
}
