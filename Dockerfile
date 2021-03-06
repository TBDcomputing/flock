# https://docs.docker.com/engine/examples/running_ssh_service/
FROM centos:7

# just run both -- only one will succeed
RUN yum install -y openssh-server; exit 0
RUN apt-get install -y openssh-server; exit 0

RUN mkdir /var/run/sshd
RUN echo 'root:screencast' | chpasswd
RUN sed -i 's/PermitRootLogin without-password/PermitRootLogin yes/' /etc/ssh/sshd_config

# SSH login fix. Otherwise user is kicked off after login
RUN sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd

ENV NOTVISIBLE "in users profile"
RUN echo "export VISIBLE=now" >> /etc/profile

# RUN su 
# RUN yes "" | ssh-keygen -t rsa -f /etc/ssh/ssh_host_rsa_key
# RUN yes "" | ssh-keygen -t dsa -f /etc/ssh/ssh_host_dsa_key
# RUN yes "" | ssh-keygen -t ecdsa -f /etc/ssh/ssh_host_ecdsa_key

EXPOSE 22

CMD ["/usr/sbin/sshd", "-D"]
