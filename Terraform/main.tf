terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0" # La versión estable actual de provider AWS
    }
  }
}

provider "aws" {
  region     = var.region_server
  access_key = var.access_key
  secret_key = var.secret_access_key
}

# Buscar la última AMI de Amazon Linux 2023 válida en la región
data "aws_ami" "amazon_linux" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-x86_64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# ==========================================
# SECURITY GROUPS
# ==========================================
# resource "aws_security_group" "frontend_sg" {
#   name        = "frontend-sg"
#   description = "Security group para la instancia EC2 de Frontend"

#   ingress {
#     description = "SSH desde cualquier lugar"
#     from_port   = 22
#     to_port     = 22
#     protocol    = "tcp"
#     cidr_blocks = ["0.0.0.0/0"]
#   }

#   ingress {
#     description = "Trafico HTTP publico"
#     from_port   = 80
#     to_port     = 80
#     protocol    = "tcp"
#     cidr_blocks = ["0.0.0.0/0"]
#   }

#   ingress {
#     description = "Trafico HTTPS publico"
#     from_port   = 443
#     to_port     = 443
#     protocol    = "tcp"
#     cidr_blocks = ["0.0.0.0/0"]
#   }

#   egress {
#     from_port        = 0
#     to_port          = 0
#     protocol         = "-1"
#     cidr_blocks      = ["0.0.0.0/0"]
#     ipv6_cidr_blocks = ["::/0"]
#   }

#   tags = {
#     Name = "frontend-sg"
#   }
# }

resource "aws_security_group" "backend_sg" {
  name        = "backend-sg"
  description = "Security group para la instancia EC2 de Backend"

  ingress {
    description = "SSH de administracion"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["168.228.132.62/32"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "backend-sg"
  }
}

# Regla para permitir llamadas desde Frontend hacia Backend en el puerto 8080
# resource "aws_security_group_rule" "allow_frontend_to_backend" {
#   type                     = "ingress"
#   from_port                = 8080
#   to_port                  = 8080
#   protocol                 = "tcp"
#   description              = "Permitir llamadas al API desde el EC2 de Frontend"
#   security_group_id        = aws_security_group.backend_sg.id
#   source_security_group_id = aws_security_group.frontend_sg.id
# }

# ==========================================
# INSTANCIAS EC2
# ==========================================
resource "aws_instance" "svr_backend_globomatic" {
  ami                    = data.aws_ami.amazon_linux.id
  instance_type          = "t3.micro"
  key_name               = var.key_name
  vpc_security_group_ids = [aws_security_group.backend_sg.id] # CORREGIDO

  tags = {
    Name = "backend-globomatic"
  }
}

# resource "aws_instance" "svr_frontend_globomatic" {
#   ami                    = data.aws_ami.amazon_linux.id
#   instance_type          = "t3.micro"
#   key_name               = var.key_name
#   vpc_security_group_ids = [aws_security_group.frontend_sg.id] # CORREGIDO

#   tags = {
#     Name = "frontend-globomatic"
#   }
# }