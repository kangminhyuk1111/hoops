# Terraform 빠른 참조 가이드

## 핵심 명령어

`terraform init` - 프로젝트 초기화 (처음 한 번)

`terraform plan` - 변경 사항 미리보기 (실제 적용 안 함)

`terraform apply` - 인프라 생성/수정

`terraform destroy` - 모든 리소스 삭제

`terraform output` - 출력값 다시 확인

---

## 배포 순서

```bash
# 1. 변수 파일 준비
cd terraform
cp terraform.tfvars.example terraform.tfvars
# terraform.tfvars 편집

# 2. 인프라 생성
terraform init
terraform apply

# 3. 애플리케이션 배포
ssh -i ~/.ssh/hoops-key.pem ec2-user@<IP>
cd ~/hoops && ./deploy.sh
```

---

## 생성되는 AWS 리소스

**VPC (hoops-vpc)** - 가상 네트워크 공간

**Subnet (hoops-public-subnet)** - 서버를 배치할 구역

**Internet Gateway (hoops-igw)** - 인터넷 연결 관문

**Security Group (hoops-app-sg)** - 방화벽 규칙

**EC2 Instance (hoops-app)** - 애플리케이션 서버

**Elastic IP (hoops-eip)** - 고정 공인 IP

---

## 열린 포트

**22 (SSH)** - 설정된 IP에서만 접근 가능

**80 (HTTP)** - 모든 IP에서 접근 가능

**443 (HTTPS)** - 모든 IP에서 접근 가능

**3000 (Frontend)** - 모든 IP에서 접근 가능

**8080 (Backend)** - 모든 IP에서 접근 가능

---

## 예상 비용 (서울 리전)

EC2 t3.small 인스턴스는 월 약 $15입니다.

EBS 30GB gp3 볼륨은 월 약 $2.4입니다.

Elastic IP는 실행 중인 인스턴스에 연결 시 무료입니다.

총합 월 $17~20 정도입니다.

---

## 유용한 AWS CLI 명령어

EC2 상태 확인:
```bash
aws ec2 describe-instances --filters "Name=tag:Project,Values=hoops"
```

EC2 중지 (비용 절약):
```bash
aws ec2 stop-instances --instance-ids <INSTANCE_ID>
```

EC2 시작:
```bash
aws ec2 start-instances --instance-ids <INSTANCE_ID>
```

---

## 서버 관리 명령어 (SSH 접속 후)

서비스 상태 확인:
```bash
docker-compose ps
```

전체 로그 확인:
```bash
docker-compose logs -f
```

백엔드 로그만:
```bash
docker-compose logs -f backend
```

서비스 재시작:
```bash
docker-compose restart
```

완전 재배포:
```bash
./deploy.sh
```

MySQL 접속:
```bash
docker exec -it hoops-mysql mysql -u root -p
```

---

## 문제 해결

**SSH 접속이 안 될 때** - 키 파일 권한을 `chmod 400`으로 설정했는지 확인하세요.

**terraform apply 실패** - `terraform init`을 다시 실행하고, AWS 자격 증명을 확인하세요.

**웹사이트 접속 안 됨** - `docker-compose ps`로 컨테이너 상태를 확인하고, `docker-compose logs`로 에러를 확인하세요.
