#! /bin/bash

print_help() {
	script_name="$(basename "$0")"
	echo "Usage:"
	echo -e "\t$script_name help\n\t\t to see this prompt."
	echo -e "\t$script_name open [PORT NUMBER]\n\t\t for opening ports for the VM (Mac only)"
	echo -e "\t$script_name vmports\n\t\t for listing the forwarding rules for the VM (Mac only)"
	echo -e "\t$script_name start [DOCKER_IMAGE] [PORT NUMBER]\n\t\t to start a new docker image with SSH available at the given port"
	echo -e "\t$script_name stop [DOCKER_IMAGE] [PORT NUMBER]\n\t\t to stop the container with the docker image/port combination"
}

if [[ $# -eq 0 ]] || [[ $1 = "help" ]]; then
	print_help
	exit
fi

# open VM ports
if [[ $1 = "open" ]]; then
	if [[ $# -ne 2 ]]; then
		echo "Please specify a port to open with no other arguments."
		exit
	fi
	ssh_port=$2
	if [ "$(uname)" == "Darwin" ]; then
	    # Do something under Mac OS X platform
	    # mac only -- only needs to be run once per port
	    vm_name=$(docker-machine active)
		docker-machine stop $vm_name
		VBoxManage modifyvm $vm_name --natpf1 "tcp-port$ssh_port,tcp,,$ssh_port,,$ssh_port"
		VBoxManage modifyvm $vm_name --natpf1 "udp-port$ssh_port,udp,,$ssh_port,,$ssh_port"
		docker-machine start $vm_name
		eval "$(docker-machine env default)"
		# stop mac only
	elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
    	# Do something under GNU/Linux platform
    	echo "Opening VM ports is not supported nor necessary on Linux."
    fi
    exit
fi

# list VM ports
if [[ $1 = "vmports" ]]; then
	if [ "$(uname)" == "Darwin" ]; then
	    # Do something under Mac OS X platform
	    # mac only
	    vm_name=$(docker-machine active)
	    VBoxManage showvminfo $vm_name --machinereadable | awk -F '[",]' '/^Forwarding/ { printf ("Rule %s host port %d forwards to guest port %d\n", $2, $5, $7); }'
		# stop mac only
	elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
    	# Do something under GNU/Linux platform
    	echo "Listing VM ports is not supported nor necessary on Linux."
    fi
    exit
fi

# start docker image
if [[ $1 = "start" ]]; then
	if [[ $# -ne 3 ]]; then
		echo "Please specify a Docker image and port to start a new container."
		exit
	fi
	# pass in docker image as arg
	OIFS=$IFS
	IFS=':' read -a myarray <<< "$2"
	IFS=$OIFS

	docker_image=${myarray[0]}
	docker_image_version=${myarray[1]}

	if [[ $docker_image_version = "" ]]; then
		docker_image_version="latest"
	fi

	ssh_port=$3

	# cleans up any old docker images, added because I accumulated so many during testing
	# docker stop $(docker ps -a -q) > /dev/null 2>/dev/null
	# docker rm $(docker ps -a -q) > /dev/null 2>/dev/null

	# this is the setup SSH server in the container method
	rm -f Dockerfile
	cp Dockerfile.example Dockerfile
	sed -i '' 's/dockerimage/'"$docker_image"':'"$docker_image_version"'/g' Dockerfile
	docker build -t "ssh_${docker_image}:${docker_image_version}" .
	docker_id=$(docker run -d --name ${ssh_port}_ssh_${docker_image} -p $ssh_port:22 ssh_${docker_image}:${docker_image_version})
	# docker attach "$docker_id"
	# su; ssh-keygen -t rsa -f /etc/ssh/ssh_host_rsa_key -N ''; ssh-keygen -t dsa -f /etc/ssh/ssh_host_dsa_key -N ''; ssh-keygen -t ecdsa -f /etc/ssh/ssh_host_ecdsa_key -N ''; exit
	yes "exit" | docker exec -i "$docker_id" /bin/bash -c "su; ssh-keygen -t rsa -f /etc/ssh/ssh_host_rsa_key -N ''; ssh-keygen -t dsa -f /etc/ssh/ssh_host_dsa_key -N ''; ssh-keygen -t ecdsa -f /etc/ssh/ssh_host_ecdsa_key -N ''; exit" > /dev/null
	# exit
	# docker exec "$docker_id" /bin/bash -c "mkdir /var/run/sshd; echo 'root:screencast' | chpasswd; sed -i 's/PermitRootLogin without-password/PermitRootLogin yes/' /etc/ssh/sshd_config; sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd; su; yes "" | ssh-keygen -t rsa -f /etc/ssh/ssh_host_rsa_key; yes "" | ssh-keygen -t dsa -f /etc/ssh/ssh_host_dsa_key; yes "" | ssh-keygen -t ecdsa -f /etc/ssh/ssh_host_ecdsa_key"

	# this is the setup SSH in another container method

	# docker_id=$(docker run -d $docker_image)
	# ssh_docker_id=$(docker run -d -p $ssh_port:22 -v /var/run/docker.sock:/var/run/docker.sock -e CONTAINER=$docker_id -e AUTH_MECHANISM=noAuth jeroenpeeters/docker-ssh)
	# ssh_docker_id=$(docker run -d -p $ssh_port:22 -v /var/run/docker.sock:/var/run/docker.sock -e CONTAINER=$docker_id -e AUTH_MECHANISM=noAuth 4b79f0b0b450)

	# ssh -p "$ssh_port" root@localhost

	# deletes the old SSH key for the given port if it exists
	ssh-keygen -R "[localhost]:$ssh_port" > /dev/null 2>/dev/null

	echo -e "You can now SSH into your container using:\n ssh -p $ssh_port root@localhost (password is screencast)"
	exit
fi

# stop docker image
if [[ $1 = "stop" ]]; then
	if [[ $# -ne 3 ]]; then
		echo "Please specify a Docker image and port to stop a container."
		exit
	fi
	# pass in docker image as arg
	OIFS=$IFS
	IFS=':' read -a myarray <<< "$2"
	IFS=$OIFS

	docker_image=${myarray[0]}
	docker_image_version=${myarray[1]}
	if [[ $docker_image_version = "" ]]; then
		docker_image_version="latest"
	fi

	ssh_port=$3

	# cleans up any old docker images, added because I accumulated so many during testing
	docker stop "${ssh_port}_ssh_${docker_image}"
	docker rm "${ssh_port}_ssh_${docker_image}"

	echo -e "Your container has been stopped and removed."
	exit
fi

print_help
exit