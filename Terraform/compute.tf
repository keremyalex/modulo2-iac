# ==========================================
# INSTANCIAS EC2
# ==========================================
resource "aws_instance" "svr_backend_globomatic" {
  ami                         = data.aws_ami.amazon_linux.id
  instance_type               = "t3.micro"
  key_name                    = var.key_name
  associate_public_ip_address = true
  vpc_security_group_ids      = [aws_security_group.backend_sg.id]

  metadata_options {
    http_tokens = "required"
  }

  tags = {
    Name = "backend-globomatic"
  }
}

resource "aws_instance" "svr_frontend_globomatic" {
  ami                         = data.aws_ami.amazon_linux.id
  instance_type               = "t3.micro"
  key_name                    = var.key_name
  associate_public_ip_address = true
  vpc_security_group_ids      = [aws_security_group.frontend_sg.id]

  metadata_options {
    http_tokens = "required"
  }

  tags = {
    Name = "frontend-globomatic"
  }
}
