output "svr_backend_globomatic_id" {
  description = "ID de la instancia EC2 Backend"
  value       = aws_instance.svr_backend_globomatic.id
}

output "svr_backend_globomatic_public_ip" {
  description = "IP publica de la instancia EC2 Backend"
  value       = aws_instance.svr_backend_globomatic.public_ip
}

# Agregada la IP privada para conectar Nginx internamente
output "svr_backend_globomatic_private_ip" {
  description = "IP privada de la instancia EC2 Backend"
  value       = aws_instance.svr_backend_globomatic.private_ip
}

output "svr_frontend_globomatic_id" {
  description = "ID de la instancia EC2 Frontend"
  value       = aws_instance.svr_frontend_globomatic.id
}

output "svr_frontend_globomatic_public_ip" {
  description = "IP publica de la instancia EC2 Frontend"
  value       = aws_instance.svr_frontend_globomatic.public_ip # CORREGIDO
}