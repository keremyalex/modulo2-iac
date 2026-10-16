variable "region_server" {
  description = "Region donde se crearan las instancias EC2"
  type        = string
  default     = "sa-east-1"
}

variable "key_name" {
  description = "Nombre del key pair existente en AWS"
  type        = string
  default     = "pk-lab2-ecommerce"
}