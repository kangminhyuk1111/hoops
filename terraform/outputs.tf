output "instance_id" {
  description = "EC2 instance ID"
  value       = aws_instance.app.id
}

output "instance_public_ip" {
  description = "Public IP address of the EC2 instance"
  value       = aws_eip.app.public_ip
}

output "ssh_command" {
  description = "SSH command to connect to the instance"
  value       = "ssh -i ~/.ssh/${var.key_name}.pem ec2-user@${aws_eip.app.public_ip}"
}

output "backend_url" {
  description = "Backend API URL"
  value       = "http://${aws_eip.app.public_ip}:8080"
}

output "frontend_url" {
  description = "Frontend URL"
  value       = "http://${aws_eip.app.public_ip}:3000"
}

output "deploy_instructions" {
  description = "Instructions to deploy the application"
  value       = <<-EOT
    1. SSH into the instance:
       ssh -i ~/.ssh/${var.key_name}.pem ec2-user@${aws_eip.app.public_ip}

    2. Run the deployment script:
       cd ~/hoops && ./deploy.sh

    3. Check service status:
       docker-compose ps

    4. View logs:
       docker-compose logs -f
  EOT
}
