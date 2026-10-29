resource "aws_ssm_parameter" "database_password" {
  name        = "/globomatics/database/password"
  description = "Contrasena de PostgreSQL para el backend de Globomatics"
  type        = "SecureString"
  value       = var.db_password

  tags = {
    Name = "globomatics-database-password"
  }
}
