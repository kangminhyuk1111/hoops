# Terraform & AWS 인프라 가이드

이 문서는 Terraform을 사용한 AWS 인프라 구축의 이론과 실제 적용을 설명합니다.

---

## 1. Terraform 기초

### 1.1 Terraform이란?

Terraform은 **Infrastructure as Code(IaC)** 도구입니다. 서버, 네트워크, 데이터베이스 같은 인프라를 코드로 정의하고 관리할 수 있게 해줍니다.

**전통적인 방식의 문제점:**

AWS 콘솔에서 마우스로 클릭해서 서버를 만들면 여러 문제가 생깁니다. 설정을 변경할 때마다 수동으로 작업해야 하고, 현재 인프라가 어떤 상태인지 추적하기 어렵습니다. 가장 큰 문제는 동일한 환경을 다시 만들기가 거의 불가능하다는 것입니다.

**Terraform이 해결하는 것:**

코드로 인프라를 정의하면 Git으로 버전 관리가 가능합니다. 누가 언제 무엇을 변경했는지 추적할 수 있고, 코드만 있으면 동일한 환경을 언제든 재현할 수 있습니다. `terraform apply` 명령어 하나로 전체 인프라가 자동으로 생성됩니다.

### 1.2 Terraform 작동 원리

Terraform은 세 단계로 작동합니다.

**1단계: 코드 작성 (.tf 파일)**

원하는 인프라를 HCL(HashiCorp Configuration Language)로 작성합니다. "EC2 인스턴스 1개, t3.small 타입, 서울 리전"처럼 선언적으로 정의합니다.

**2단계: 실행 계획 (terraform plan)**

Terraform이 현재 상태와 원하는 상태를 비교해서 무엇을 생성/수정/삭제할지 보여줍니다. 실제로 변경하지는 않습니다.

**3단계: 적용 (terraform apply)**

계획을 실제로 실행해서 AWS에 인프라를 생성합니다. 완료 후 현재 상태를 `terraform.tfstate` 파일에 저장합니다.

### 1.3 핵심 명령어

`terraform init`은 프로젝트를 초기화합니다. 필요한 provider(AWS 연결 모듈)를 다운로드합니다. 프로젝트당 처음 한 번만 실행하면 됩니다.

`terraform plan`은 변경 사항을 미리 봅니다. 실제로 적용하지 않고 무엇이 바뀔지만 보여줍니다. 실수를 방지하기 위해 apply 전에 항상 실행하세요.

`terraform apply`는 인프라를 실제로 생성하거나 수정합니다. 확인 프롬프트에서 "yes"를 입력해야 실행됩니다.

`terraform destroy`는 Terraform으로 만든 모든 리소스를 삭제합니다. 비용 절약을 위해 테스트 후 정리할 때 사용합니다.

### 1.4 파일 구조

Terraform 프로젝트는 여러 `.tf` 파일로 구성됩니다. 파일 이름은 자유롭게 정할 수 있지만, 관례적으로 역할별로 나눕니다.

`main.tf`는 provider 설정을 담습니다. AWS와 연결하기 위한 기본 설정입니다.

`variables.tf`는 변수를 정의합니다. 인스턴스 타입, 리전 같은 설정값을 변수로 만들어서 재사용합니다.

`outputs.tf`는 출력값을 정의합니다. 생성된 서버의 IP 주소 같은 정보를 출력합니다.

`terraform.tfvars`는 실제 변수값을 저장합니다. 비밀번호 같은 민감한 정보가 들어가므로 Git에 커밋하면 안 됩니다.

### 1.5 기본 문법

**리소스 생성:**

```hcl
resource "aws_instance" "my_server" {
  ami           = "ami-12345678"
  instance_type = "t3.small"
}
```

`resource`는 새로운 인프라를 만들겠다는 선언입니다. `"aws_instance"`는 만들려는 리소스 타입(EC2 인스턴스)이고, `"my_server"`는 이 리소스를 부르는 이름입니다. 중괄호 안에 설정값을 넣습니다.

**변수 사용:**

```hcl
variable "instance_type" {
  description = "EC2 인스턴스 타입"
  default     = "t3.small"
}

resource "aws_instance" "app" {
  instance_type = var.instance_type
}
```

`variable`로 변수를 정의하고, `var.변수명`으로 사용합니다. 같은 값을 여러 곳에서 쓸 때 유용합니다.

**리소스 참조:**

```hcl
resource "aws_vpc" "main" {
  cidr_block = "10.0.0.0/16"
}

resource "aws_subnet" "public" {
  vpc_id = aws_vpc.main.id
}
```

`aws_vpc.main.id`처럼 다른 리소스의 속성을 참조할 수 있습니다. Terraform이 자동으로 의존성을 파악해서 VPC를 먼저 만들고 Subnet을 만듭니다.

---

## 2. AWS 핵심 개념

### 2.1 네트워크 구성 개요

AWS에서 서버를 만들려면 먼저 네트워크를 구성해야 합니다. 집을 짓기 전에 땅을 마련하는 것과 같습니다.

### 2.2 VPC (Virtual Private Cloud)

VPC는 AWS 안에 만드는 우리만의 격리된 네트워크 공간입니다. 건물 전체라고 생각하면 됩니다. 다른 AWS 사용자의 네트워크와 완전히 분리되어 있습니다.

VPC를 만들 때 IP 주소 범위를 지정합니다. 예를 들어 `10.0.0.0/16`은 `10.0.x.x` 범위의 약 65,000개 IP를 사용하겠다는 의미입니다.

### 2.3 Subnet (서브넷)

Subnet은 VPC를 더 작은 구역으로 나눈 것입니다. 건물의 층이나 구역이라고 생각하면 됩니다.

**Public Subnet**은 인터넷과 통신할 수 있는 구역입니다. 웹 서버처럼 외부 접근이 필요한 리소스를 배치합니다.

**Private Subnet**은 인터넷에서 직접 접근할 수 없는 구역입니다. 데이터베이스처럼 보안이 중요한 리소스를 배치합니다. 우리 프로젝트에서는 비용 절감을 위해 Public Subnet 하나만 사용합니다.

### 2.4 Internet Gateway

Internet Gateway는 VPC와 인터넷을 연결하는 관문입니다. 건물의 정문이라고 생각하면 됩니다. 이게 없으면 VPC 안의 서버가 인터넷과 통신할 수 없습니다.

### 2.5 Route Table

Route Table은 네트워크 트래픽이 어디로 가야 하는지 정의하는 이정표입니다. "외부로 나가는 트래픽(`0.0.0.0/0`)은 Internet Gateway로 보내라"처럼 규칙을 설정합니다.

### 2.6 Security Group

Security Group은 서버의 가상 방화벽입니다. 어떤 트래픽을 허용하고 차단할지 결정합니다.

**Inbound 규칙**은 외부에서 서버로 들어오는 트래픽을 제어합니다. 예를 들어 "포트 22(SSH)는 내 IP에서만 허용", "포트 3000(웹)은 모든 IP에서 허용"처럼 설정합니다.

**Outbound 규칙**은 서버에서 외부로 나가는 트래픽을 제어합니다. 보통 모든 아웃바운드를 허용합니다.

### 2.7 CIDR 표기법

CIDR은 IP 주소 범위를 표현하는 방법입니다.

`10.0.0.0/16`에서 `/16`은 앞의 16비트(10.0)가 고정이라는 뜻입니다. 나머지 비트로 `10.0.0.0`부터 `10.0.255.255`까지 약 65,000개 IP를 사용할 수 있습니다.

`10.0.1.0/24`에서 `/24`는 앞의 24비트(10.0.1)가 고정입니다. `10.0.1.0`부터 `10.0.1.255`까지 256개 IP를 사용할 수 있습니다.

`0.0.0.0/0`은 "모든 IP"를 의미합니다. Security Group에서 인터넷 전체를 허용할 때 사용합니다.

### 2.8 EC2 (Elastic Compute Cloud)

EC2는 AWS의 가상 서버입니다. 원하는 사양의 컴퓨터를 클라우드에서 빌려 쓴다고 생각하면 됩니다.

**AMI (Amazon Machine Image)**는 서버의 운영체제 이미지입니다. Amazon Linux, Ubuntu 등을 선택할 수 있습니다. 우리는 Amazon Linux 2023을 사용합니다.

**Instance Type**은 서버의 사양입니다. t3.small은 vCPU 2개, 메모리 2GB입니다. 소규모 애플리케이션에 적합하고 월 약 $15입니다.

**Key Pair**는 SSH 접속용 암호화 키입니다. 비밀번호 대신 키 파일로 서버에 접속합니다. 키 파일을 분실하면 서버에 접속할 수 없으니 안전하게 보관하세요.

**EBS (Elastic Block Store)**는 서버의 하드디스크입니다. 용량과 타입을 지정할 수 있습니다. gp3 타입 30GB면 대부분의 애플리케이션에 충분합니다.

**Elastic IP**는 고정 공인 IP입니다. EC2 인스턴스를 재시작해도 IP가 변하지 않습니다. 도메인을 연결하거나 안정적인 접속이 필요할 때 사용합니다.

### 2.9 User Data

User Data는 EC2 인스턴스가 처음 시작될 때 자동으로 실행되는 스크립트입니다. Docker 설치, 환경 설정 같은 초기화 작업을 자동화할 수 있습니다.

```bash
#!/bin/bash
dnf update -y
dnf install -y docker
systemctl start docker
```

이렇게 작성하면 서버가 부팅될 때 자동으로 Docker가 설치됩니다.

---

## 3. Hoops 인프라 구성

### 3.1 아키텍처 설명

우리 인프라는 단순함을 추구합니다. 하나의 EC2 인스턴스 안에서 Docker Compose로 모든 서비스를 실행합니다.

EC2 인스턴스 안에는 세 개의 Docker 컨테이너가 실행됩니다. MySQL 컨테이너는 데이터베이스를 담당하고, Backend 컨테이너는 Spring Boot API 서버를 실행하며, Frontend 컨테이너는 Next.js 웹 애플리케이션을 제공합니다.

사용자가 웹사이트에 접속하면 Frontend(포트 3000)가 응답합니다. Frontend는 Backend(포트 8080)에 API를 호출하고, Backend는 MySQL(포트 3306)에서 데이터를 조회합니다.

### 3.2 RDS를 사용하지 않는 이유

AWS RDS는 관리형 데이터베이스 서비스입니다. 자동 백업, 패치, 복제 같은 편의 기능을 제공하지만 추가 비용이 발생합니다. 가장 작은 db.t3.micro도 월 $15 정도입니다.

소규모 프로젝트에서는 EC2 안에서 Docker로 MySQL을 실행해도 충분합니다. 나중에 트래픽이 늘거나 안정성이 중요해지면 그때 RDS로 마이그레이션하면 됩니다.

### 3.3 예상 비용

EC2 t3.small 인스턴스는 24시간 가동 기준 월 약 $15입니다. EBS 30GB gp3 볼륨은 월 약 $2.4입니다. Elastic IP는 실행 중인 인스턴스에 연결되어 있으면 무료입니다. 데이터 전송 비용은 사용량에 따라 다르지만, 소규모 서비스라면 월 $10 이내입니다.

총합하면 월 $20~30 정도로 운영할 수 있습니다. RDS를 추가하면 $15~30이 더 들기 때문에, EC2 내 MySQL로 절반 가까이 절약합니다.

### 3.4 확장 가능성

현재 구성은 소규모 서비스에 적합합니다. 트래픽이 늘면 다음과 같이 확장할 수 있습니다.

인스턴스 타입을 t3.medium, t3.large로 업그레이드할 수 있습니다. Terraform에서 `instance_type` 변수만 바꾸면 됩니다.

MySQL을 RDS로 분리하면 데이터베이스 성능과 안정성이 향상됩니다. 별도의 Terraform 리소스를 추가하면 됩니다.

여러 EC2 인스턴스를 만들고 ALB(Application Load Balancer)로 트래픽을 분산할 수 있습니다.

---

## 4. 파일별 상세 설명

### 4.1 main.tf

```hcl
terraform {
  required_version = ">= 1.0.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "hoops"
      Environment = var.environment
      ManagedBy   = "terraform"
    }
  }
}
```

`terraform` 블록은 Terraform 자체의 설정입니다. 최소 버전 1.0.0이 필요하고, AWS provider 버전 5.x를 사용합니다.

`provider "aws"` 블록은 AWS와 연결하는 설정입니다. `region`으로 서울 리전(ap-northeast-2)을 지정합니다. `default_tags`는 모든 리소스에 자동으로 붙는 태그입니다. 나중에 비용 분석이나 리소스 관리에 유용합니다.

### 4.2 variables.tf

```hcl
variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-northeast-2"
}

variable "key_name" {
  description = "EC2 key pair name for SSH access"
  type        = string
}

variable "jwt_secret" {
  description = "JWT secret key (min 32 characters)"
  type        = string
  sensitive   = true
}
```

각 변수는 `description`(설명), `type`(타입), `default`(기본값)를 가집니다.

`default`가 있는 변수는 값을 안 넣어도 기본값이 적용됩니다. `default`가 없는 변수는 반드시 `terraform.tfvars`에 값을 넣어야 합니다.

`sensitive = true`는 민감한 정보를 표시합니다. `terraform plan`이나 로그에서 값이 숨겨집니다.

### 4.3 vpc.tf

```hcl
resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name = "${var.project_name}-vpc"
  }
}
```

VPC를 생성합니다. `cidr_block`은 IP 범위입니다. `enable_dns_hostnames`와 `enable_dns_support`를 켜야 EC2 인스턴스가 DNS 이름을 받습니다.

```hcl
resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id
}
```

Internet Gateway를 만들고 VPC에 연결합니다. `aws_vpc.main.id`로 위에서 만든 VPC를 참조합니다.

```hcl
resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = var.public_subnet_cidr
  map_public_ip_on_launch = true
}
```

Public Subnet을 만듭니다. `map_public_ip_on_launch = true`는 이 서브넷에서 실행되는 EC2에 자동으로 공인 IP를 할당합니다.

```hcl
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }
}

resource "aws_route_table_association" "public" {
  subnet_id      = aws_subnet.public.id
  route_table_id = aws_route_table.public.id
}
```

Route Table을 만들고 "모든 외부 트래픽(0.0.0.0/0)은 Internet Gateway로"라는 규칙을 추가합니다. 그리고 이 Route Table을 Public Subnet에 연결합니다.

### 4.4 security.tf

```hcl
resource "aws_security_group" "app" {
  name   = "${var.project_name}-app-sg"
  vpc_id = aws_vpc.main.id

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.allowed_ssh_cidr]
  }

  ingress {
    description = "Frontend"
    from_port   = 3000
    to_port     = 3000
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "Backend API"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}
```

Security Group에서 `ingress`는 들어오는 트래픽, `egress`는 나가는 트래픽입니다.

SSH(22번 포트)는 `var.allowed_ssh_cidr`에 지정된 IP만 허용합니다. 보안을 위해 자신의 IP만 허용하는 것이 좋습니다.

Frontend(3000)와 Backend(8080)는 모든 IP(`0.0.0.0/0`)에서 접근 가능합니다.

`protocol = "-1"`은 모든 프로토콜을 의미합니다. egress에서 이렇게 설정하면 서버가 외부로 모든 통신을 할 수 있습니다.

### 4.5 ec2.tf

```hcl
data "aws_ami" "amazon_linux" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }
}
```

`data` 블록은 기존 리소스를 조회합니다. 여기서는 가장 최신 Amazon Linux 2023 AMI ID를 자동으로 찾습니다. AMI ID는 리전마다 다르고 수시로 업데이트되기 때문에, 하드코딩하지 않고 동적으로 조회하는 것이 좋습니다.

```hcl
resource "aws_instance" "app" {
  ami                    = data.aws_ami.amazon_linux.id
  instance_type          = var.instance_type
  key_name               = var.key_name
  subnet_id              = aws_subnet.public.id
  vpc_security_group_ids = [aws_security_group.app.id]

  root_block_device {
    volume_size = var.ebs_volume_size
    volume_type = "gp3"
    encrypted   = true
  }

  user_data = base64encode(templatefile("${path.module}/scripts/user-data.sh", {
    mysql_root_password = var.mysql_root_password
    kakao_client_id     = var.kakao_client_id
    jwt_secret          = var.jwt_secret
  }))
}
```

EC2 인스턴스를 생성합니다. `ami`는 위에서 조회한 Amazon Linux 이미지를 사용합니다. `subnet_id`로 어떤 서브넷에 배치할지, `vpc_security_group_ids`로 어떤 방화벽 규칙을 적용할지 지정합니다.

`root_block_device`는 EBS 볼륨(하드디스크) 설정입니다. 30GB gp3 타입으로 암호화를 활성화합니다.

`user_data`는 초기화 스크립트입니다. `templatefile` 함수로 스크립트에 변수를 주입합니다. 서버가 처음 부팅될 때 Docker가 설치되고 환경 변수가 설정됩니다.

```hcl
resource "aws_eip" "app" {
  instance = aws_instance.app.id
  domain   = "vpc"
}
```

Elastic IP를 만들고 EC2에 연결합니다. 서버를 재시작해도 IP가 변하지 않습니다.

### 4.6 outputs.tf

```hcl
output "instance_public_ip" {
  description = "Public IP address"
  value       = aws_eip.app.public_ip
}

output "ssh_command" {
  description = "SSH command"
  value       = "ssh -i ~/.ssh/${var.key_name}.pem ec2-user@${aws_eip.app.public_ip}"
}
```

`output`은 `terraform apply` 후 터미널에 출력되는 값입니다. 서버 IP, SSH 명령어, URL 등 유용한 정보를 출력하도록 설정합니다.

---

## 5. 배포 실습

### 5.1 사전 준비

**AWS CLI 설치 및 설정**

먼저 AWS CLI를 설치합니다. macOS에서는 `brew install awscli`로 설치할 수 있습니다.

설치 후 `aws configure` 명령어로 자격 증명을 설정합니다. AWS IAM에서 발급받은 Access Key와 Secret Key를 입력하고, 리전은 `ap-northeast-2`(서울)를 입력합니다.

**Terraform 설치**

macOS에서는 `brew install terraform`으로 설치합니다. `terraform version`으로 설치를 확인합니다.

**EC2 Key Pair 생성**

SSH 접속에 사용할 키 페어를 만듭니다.

```bash
aws ec2 create-key-pair \
  --key-name hoops-key \
  --query 'KeyMaterial' \
  --output text > ~/.ssh/hoops-key.pem

chmod 400 ~/.ssh/hoops-key.pem
```

`chmod 400`은 필수입니다. 권한이 너무 열려 있으면 SSH가 키 파일을 거부합니다.

### 5.2 변수 설정

```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars
```

`terraform.tfvars` 파일을 열어서 실제 값을 입력합니다.

```hcl
aws_region   = "ap-northeast-2"
key_name     = "hoops-key"
instance_type = "t3.small"

kakao_client_id     = "실제_카카오_클라이언트_ID"
kakao_client_secret = "실제_카카오_시크릿"
jwt_secret          = "32자_이상의_안전한_문자열"
mysql_root_password = "안전한_MySQL_비밀번호"
```

이 파일은 `.gitignore`에 포함되어 있어서 Git에 커밋되지 않습니다.

### 5.3 인프라 생성

```bash
terraform init
```

처음 한 번 실행합니다. AWS provider가 다운로드됩니다.

```bash
terraform plan
```

무엇이 생성될지 미리 봅니다. "Plan: 8 to add"처럼 생성될 리소스 수가 표시됩니다.

```bash
terraform apply
```

실제로 인프라를 생성합니다. 확인 프롬프트가 나오면 `yes`를 입력합니다. 완료까지 2~3분 정도 걸립니다.

완료되면 출력값이 표시됩니다. `instance_public_ip`가 서버의 공인 IP입니다.

### 5.4 애플리케이션 배포

인프라가 생성되면 SSH로 서버에 접속합니다.

```bash
ssh -i ~/.ssh/hoops-key.pem ec2-user@<PUBLIC_IP>
```

처음 접속하면 "fingerprint" 확인을 요청합니다. `yes`를 입력합니다.

서버에 접속한 후 배포 스크립트를 실행합니다.

```bash
cd ~/hoops
./deploy.sh
```

스크립트가 GitHub에서 코드를 클론하고, Docker 이미지를 빌드하고, 컨테이너를 시작합니다. 처음 실행 시 이미지 빌드 때문에 10~15분 정도 걸릴 수 있습니다.

### 5.5 확인 및 관리

배포 완료 후 서비스 상태를 확인합니다.

```bash
docker-compose ps
```

모든 컨테이너가 "Up" 상태면 정상입니다.

로그를 보려면 다음 명령어를 사용합니다.

```bash
docker-compose logs -f
docker-compose logs -f backend  # 백엔드만
```

브라우저에서 `http://<PUBLIC_IP>:3000`으로 접속해서 웹사이트가 뜨는지 확인합니다.

### 5.6 인프라 삭제

테스트가 끝나면 비용 절약을 위해 인프라를 삭제합니다.

```bash
terraform destroy
```

확인 프롬프트에서 `yes`를 입력하면 모든 리소스가 삭제됩니다.

---

## 6. 문제 해결

### SSH 접속이 안 될 때

키 파일 권한을 확인하세요. `chmod 400 ~/.ssh/hoops-key.pem`으로 설정해야 합니다.

Security Group에서 22번 포트가 자신의 IP에서 허용되어 있는지 확인하세요. `allowed_ssh_cidr` 변수에 자신의 IP를 설정했는지 봅니다.

### terraform apply가 실패할 때

`terraform init`을 다시 실행해보세요. provider가 제대로 설치되지 않았을 수 있습니다.

AWS 자격 증명이 올바른지 확인하세요. `aws sts get-caller-identity`로 현재 계정을 확인할 수 있습니다.

### 웹사이트에 접속이 안 될 때

컨테이너가 실행 중인지 `docker-compose ps`로 확인합니다.

로그에서 에러가 있는지 `docker-compose logs`로 확인합니다.

Security Group에서 3000, 8080 포트가 열려 있는지 확인합니다.

### terraform.tfstate를 삭제했을 때

이 파일은 Terraform이 현재 인프라 상태를 추적하는 데 사용합니다. 삭제하면 Terraform이 기존 리소스를 "모른다"고 판단하고 새로 만들려고 합니다.

AWS 콘솔에서 수동으로 리소스를 삭제하거나, `terraform import` 명령어로 기존 리소스를 다시 연결해야 합니다. 복잡하니 tfstate 파일은 절대 삭제하지 마세요.
