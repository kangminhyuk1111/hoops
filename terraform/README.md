# Hoops AWS Infrastructure

Terraform configuration for deploying Hoops application to AWS EC2.

## Architecture

Single EC2 instance running all services via Docker:
- MySQL 8.0 (database)
- Spring Boot backend (port 8080)
- Next.js frontend (port 3000)

## Prerequisites

1. AWS CLI configured with appropriate credentials
2. Terraform >= 1.0.0
3. EC2 Key Pair created in AWS Console

## Quick Start

### 1. Create EC2 Key Pair

```bash
# Create key pair in AWS Console or via CLI
aws ec2 create-key-pair --key-name hoops-key --query 'KeyMaterial' --output text > ~/.ssh/hoops-key.pem
chmod 400 ~/.ssh/hoops-key.pem
```

### 2. Configure Variables

```bash
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars with your values
```

### 3. Deploy Infrastructure

```bash
terraform init
terraform plan
terraform apply
```

### 4. Deploy Application

After infrastructure is created:

```bash
# SSH into the instance (command shown in terraform output)
ssh -i ~/.ssh/hoops-key.pem ec2-user@<PUBLIC_IP>

# Run deployment script
cd ~/hoops && ./deploy.sh
```

## Variables

| Name | Description | Required |
|------|-------------|----------|
| `key_name` | EC2 Key Pair name | Yes |
| `kakao_client_id` | Kakao OAuth client ID | Yes |
| `jwt_secret` | JWT secret (min 32 chars) | Yes |
| `mysql_root_password` | MySQL root password | Yes |
| `instance_type` | EC2 instance type | No (default: t3.small) |
| `aws_region` | AWS region | No (default: ap-northeast-2) |

## Outputs

After `terraform apply`:
- `instance_public_ip` - EC2 public IP
- `backend_url` - Backend API URL
- `frontend_url` - Frontend URL
- `ssh_command` - SSH command to connect

## Estimated Cost

- t3.small EC2: ~$15/month
- 30GB gp3 EBS: ~$2.4/month
- Elastic IP: Free (when attached to running instance)
- **Total: ~$17.4/month**

## Maintenance Commands

```bash
# SSH into instance
ssh -i ~/.ssh/hoops-key.pem ec2-user@<PUBLIC_IP>

# View logs
cd ~/hoops && docker-compose logs -f

# Restart services
cd ~/hoops && docker-compose restart

# Update and redeploy
cd ~/hoops && ./deploy.sh
```

## Destroy Infrastructure

```bash
terraform destroy
```
