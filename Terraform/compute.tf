# ==========================================
# INSTANCIAS EC2
# ==========================================
resource "aws_instance" "svr_backend_globomatic" {
  ami                         = data.aws_ami.amazon_linux.id
  instance_type               = var.backend_instance_type
  key_name                    = var.key_name
  associate_public_ip_address = true
  vpc_security_group_ids      = [aws_security_group.backend_sg.id]
  iam_instance_profile        = aws_iam_instance_profile.backend.name

  user_data = templatefile("${path.module}/cloud-init/backend.yaml.tftpl", {
    repository_url    = var.repository_url
    repository_branch = var.repository_branch
    db_parameter_name = aws_ssm_parameter.database_password.name
    aws_region        = var.region_server
  })

  user_data_replace_on_change = true

  depends_on = [aws_iam_role_policy.backend_parameter_access]

  metadata_options {
    http_tokens = "required"
  }

  tags = {
    Name = "backend-tf-globomatic"
  }
}

resource "aws_instance" "svr_frontend_globomatic" {
  ami                         = data.aws_ami.amazon_linux.id
  instance_type               = var.frontend_instance_type
  key_name                    = var.key_name
  associate_public_ip_address = true
  vpc_security_group_ids      = [aws_security_group.frontend_sg.id]

  user_data = templatefile("${path.module}/cloud-init/frontend.yaml.tftpl", {
    repository_url     = var.repository_url
    repository_branch  = var.repository_branch
    backend_private_ip = aws_instance.svr_backend_globomatic.private_ip
  })

  user_data_replace_on_change = true

  metadata_options {
    http_tokens = "required"
  }

  tags = {
    Name = "frontend-tf-globomatic"
  }
}
