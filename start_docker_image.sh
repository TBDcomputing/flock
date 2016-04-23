#! /bin/bash

# pass in docker image as arg
docker_image=$1
ssh_port=8895
vm_name=$(docker-machine active)

# mac only
docker-machine stop "$vm_name"
VBoxManage modifyvm "$vm_name" --natpf1 "tcp-port$ssh_port,tcp,,$ssh_port,,$ssh_port"
VBoxManage modifyvm "$vm_name" --natpf1 "udp-port$ssh_port,udp,,$ssh_port,,$ssh_port"
docker-machine start "$vm_name"
eval "$(docker-machine env default)"
# stop mac only

vm_ip=$(docker-machine ip)

# this is the setup SSH server in the container method

# docker_id=$(docker run -d -p $ssh_port:22 $docker_image)
# docker exec "$docker_id" /bin/bash -c "yum install -y openssh-server; mkdir /var/run/sshd; echo 'root:screencast' | chpasswd; sed -i 's/PermitRootLogin without-password/PermitRootLogin yes/' /etc/ssh/sshd_config; echo \"sshd: ALL\" >> /etc/hosts.allow; /usr/sbin/sshd -D"

# this is the setup SSH in another container method

docker_id=$(docker run -d $docker_image)
ssh_docker_id=$(docker run -d -p $ssh_port:22 -v /var/run/docker.sock:/var/run/docker.sock -e CONTAINER=$docker_id -e AUTH_MECHANISM=noAuth jeroenpeeters/docker-ssh)
# ssh_docker_id=$(docker run -d -p $ssh_port:22 -v /var/run/docker.sock:/var/run/docker.sock -e CONTAINER=$docker_id -e AUTH_MECHANISM=noAuth 4b79f0b0b450)

ssh -p "$ssh_port" root@localhost