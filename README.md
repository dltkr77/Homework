### Project Directory 만들기
Command 창을 열어서 아래와 같이 디렉토리를 만들고, 해당 디렉토리로 이동합니다.
vagrant init을 통해 초기화를 해주고, vagrant box add 명령어 사용해서 박스를 추가해 줍니다.
```
cd \
md project
cd project
vagrant init
vagrant box add ubuntu/trusty64
vagrant box list

========== Logs ==========
C:\Project>vagrant box list
ubuntu/trusty64 (virtualbox, 14.04)
```

### Vagrantfile 설정
위에서 만들어진 Vagrantfile을 열어, 아래와 같이 편집합니다.
```
# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|
  # Master node
  config.vm.define "master" do |master|
    master.vm.provider "virtualbox" do |v|
      v.name = "master"
      v.memory = 4096
      v.cpus = 1
    end
    master.vm.box = "ubuntu/trusty64"
    master.vm.hostname = "master"
    master.vm.network "private_network", ip: "192.168.200.2"
    master.vm.provision "shell", path: "./setup.sh"
  end

  # Slave1 node
  config.vm.define "slave1" do |slave1|
    slave1.vm.provider "virtualbox" do |v|
      v.name = "slave1"
      v.memory = 2048
      v.cpus = 1
    end
    slave1.vm.box = "ubuntu/trusty64"
    slave1.vm.hostname = "slave1"
    slave1.vm.network "private_network", ip: "192.168.200.10"
    slave1.vm.provision "shell", path: "./setup.sh"
  end

  config.vm.define "slave2" do |slave2|
    slave2.vm.provider "virtualbox" do |v|
      v.name = "slave2"
      v.memory = 2048
      v.cpus = 1
    end
    slave2.vm.box = "ubuntu/trusty64"
    slave2.vm.hostname = "slave2"
    slave2.vm.network "private_network", ip: "192.168.200.11"
    slave2.vm.provision "shell", path: "./setup.sh"
  end
end
```

### Shell Script 작성
VM에서 사용될 환경을 만들어 주기 위한 Shell Script를 아래와 같이 작성합니다.
해당 파일명은 'setup.sh'로 만들어서 project 디렉토리에 넣어줍니다.
```
#!/bin/bash

# Variables
tools=/home/hadoop/tools
JH=/home/hadoop/tools/jdk
HH=/home/hadoop/tools/hadoop

# Install jdk
apt-get install -y openjdk-7-jre-headless
apt-get install -y openjdk-7-jdk

# Install expect
apt-get install -y expect

# Add group and user
addgroup hadoop
useradd -g hadoop -d /home/hadoop/ -s /bin/bash -m hadoop
expect << EOF
    spawn passwd hadoop
    expect "Enter new UNIX password:"
        send "hadoop"
    expect "Retype new UNIX password:"
        send "hadoop"
    expect eof
EOF

# hadoop user's password change
host=`hostname`
if [ $host == "master" ]; then
	mkdir -p /home/hadoop/hdfs/name
else
	mkdir -p /home/hadoop/hdfs/data
fi

# Download hadoop
mkdir $tools
cd $tools
wget http://ftp.daum.net/apache//hadoop/common/hadoop-1.2.1/hadoop-1.2.1.tar.gz
tar xvf hadoop-1.2.1.tar.gz
ln -s $tools/hadoop-1.2.1 $tools/hadoop
ln -s /usr/lib/jvm/java-1.7.0-openjdk-amd64 $tools/jdk
chown -R hadoop:hadoop /home/hadoop
chmod 755 -R /home/hadoop

# Environment Setting
echo "" >> ~hadoop/.bashrc
echo "export JAVA_HOME=$JH" >> ~hadoop/.bashrc
echo "export PATH=\$PATH:\$JAVA_HOME/bin:\$HH/bin" >> ~hadoop/.bashrc

# /etc/hosts Setting
echo "fe00::0 ip6-localnet" > /etc/hosts
echo "ff00::0 ip6-mcastprefix" >> /etc/hosts
echo "ff02::1 ip6-allnodes" >> /etc/hosts
echo "ff02::2 ip6-allrouters" >> /etc/hosts
echo "ff02::3 ip6-allhosts" >> /etc/hosts
echo "192.168.200.2 master" >> /etc/hosts
echo "192.168.200.10 slave1" >> /etc/hosts
echo "192.168.200.11 slave2" >> /etc/hosts
```

### vagrant up!
해당 Vagrantfile에서 사용할 VM들의 이름은 각각 'master', 'slave1', 'slave2' 입니다. 혹시 동일한 이름의 VM이 존재한다면 Vagrantfile의 v.name을 수정해야 합니다.
```
cd \project
vagrant up

========== Logs ==========
C:\Project>vagrant up
Bringing machine 'master' up with 'virtualbox' provider...
Bringing machine 'slave1' up with 'virtualbox' provider...
Bringing machine 'slave2' up with 'virtualbox' provider...
==> master: Importing base box 'ubuntu/trusty64'...
( 중략 ) // JDK 등의 설치 작업으로 시간이 소요됩니다.
==> slave2: hadoop-1.2.1/src/test/system/java/org/apache/hadoop/mapred/TestTaskT
rackerInfoSuccessfulFailedJobs.java
==> slave2: hadoop-1.2.1/src/test/system/java/org/apache/hadoop/mapred/TestTaskT
rackerInfoTTProcess.java
==> slave2: hadoop-1.2.1/src/test/system/java/org/apache/hadoop/mapreduce/test/s
ystem/FinishTaskControlAction.java
==> slave2: hadoop-1.2.1/src/test/system/java/org/apache/hadoop/mapreduce/test/s
ystem/JTClient.java
==> slave2: hadoop-1.2.1/src/test/system/java/org/apac
```

### 환경을 알아두고 넘어갑시다!
VM들의 SSH Port와 각 계정 설정은 아래와 같습니다.
```
master : 2222 Port
slave1 : 2200 Port
slave2 : 2201 Port
※ 주의사항 : 해당 포트들이 사용되고 있는지 꼭! 확인하세요.
master -> slave1 -> slave2 순서로 포트가 할당이 되며 순서는 아래와 같습니다.
2222 -> 2200 -> 2201 -> 2202 -> 2203 -> 2204...
중간에 사용이 되고 있는 port가 있을 경우, 다음 번호를 사용하게 됩니다. (즉 2222포트가 사용중이라면 master의 포트는 2200이 됩니다.)

========== 아이디 : 패스워드 ==========
root : vagant
vagrant : vagrant
hadoop : hadoop
```