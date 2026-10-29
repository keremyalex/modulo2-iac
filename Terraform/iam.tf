data "aws_iam_policy_document" "backend_assume_role" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "backend" {
  name               = "globomatics-backend-role"
  assume_role_policy = data.aws_iam_policy_document.backend_assume_role.json
}

data "aws_iam_policy_document" "backend_parameter_access" {
  statement {
    sid       = "ReadDatabasePassword"
    effect    = "Allow"
    actions   = ["ssm:GetParameter"]
    resources = [aws_ssm_parameter.database_password.arn]
  }
}

resource "aws_iam_role_policy" "backend_parameter_access" {
  name   = "read-globomatics-database-password"
  role   = aws_iam_role.backend.id
  policy = data.aws_iam_policy_document.backend_parameter_access.json
}

resource "aws_iam_instance_profile" "backend" {
  name = "globomatics-backend-profile"
  role = aws_iam_role.backend.name
}
