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
    master.vm.provision "file", source: "./ssh_setting.sh",
      destination: "/home/vagrant/ssh_setting.sh"
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
    slave1.vm.provision "file", source: "./ssh_setting.sh",
      destination: "/home/vagrant/ssh_setting.sh"
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
    slave2.vm.provision "file", source: "./ssh_setting.sh",
      destination: "/home/vagrant/ssh_setting.sh"
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

# Install git
apt-get install -y git

# Add group and user
addgroup hadoop
useradd -g hadoop -d /home/hadoop/ -s /bin/bash -m hadoop
echo -e "hadoop\nhadoop" | (passwd hadoop)

# Make directory for hdfs
host=`hostname`
if [ $host == "master" ]; then
	mkdir -p /home/hadoop/hdfs/name
else
	mkdir -p /home/hadoop/hdfs/data
fi

# Modify ssh_setting.sh(encoding problem)
sed -i 's/\r//' /home/vagrant/ssh_setting.sh
cp /home/vagrant/ssh_setting.sh /home/hadoop/

# Download hadoop
mkdir $tools
cd $tools
wget http://ftp.daum.net/apache//hadoop/common/hadoop-1.2.1/hadoop-1.2.1.tar.gz
tar xvf hadoop-1.2.1.tar.gz
ln -s $tools/hadoop-1.2.1 $tools/hadoop
ln -s /usr/lib/jvm/java-1.7.0-openjdk-amd64 $tools/jdk

# Download Maven
cd $tools
wget http://mirror.apache-kr.org/maven/maven-3/3.2.5/binaries/apache-maven-3.2.5-bin.tar.gz
tar xvf apache-maven-3.2.5-bin.tar.gz
ln -s $tools/apache-maven-3.2.5 $tools/maven

#== Hadoop Setting ==#
# hadoop-env.sh
echo "export JAVA_HOME=/home/hadoop/tools/jdk" >> $HH/conf/hadoop-env.sh
echo "export HADOOP_HOME_WARN_SUPRESS=\"TRUE\"" >> $HH/conf/hadoop-env.sh
echo "export HADOOP_OPTS=-server" >> $HH/conf/hadoop-env.sh

# core-site.xml
echo "<?xml version=\"1.0\"?>" > $HH/conf/core-site.xml
echo "<?xml-stylesheet type=\"text/xsl\" href=\"configuration.xsl\"?>" >> $HH/conf/core-site.xml
echo "" >> $HH/conf/core-site.xml
echo "<!-- Put site-specific property overrides in this file. -->" >> $HH/conf/core-site.xml
echo "" >> $HH/conf/core-site.xml
echo "<configuration>" >> $HH/conf/core-site.xml
echo "  <property>" >> $HH/conf/core-site.xml
echo "    <name>fs.default.name</name>" >> $HH/conf/core-site.xml
echo "    <value>hdfs://master:9000</value>" >> $HH/conf/core-site.xml
echo "  </property>" >> $HH/conf/core-site.xml
echo "</configuration>" >> $HH/conf/core-site.xml

# hdfs-site.xml
echo "<?xml version=\"1.0\"?>" > $HH/conf/hdfs-site.xml
echo "<?xml-stylesheet type=\"text/xsl\" href=\"configuration.xsl\"?>" >> $HH/conf/hdfs-site.xml
echo "" >> $HH/conf/hdfs-site.xml
echo "<!-- Put site-specific property overrides in this file. -->" >> $HH/conf/hdfs-site.xml
echo "" >> $HH/conf/hdfs-site.xml
echo "<configuration>" >> $HH/conf/hdfs-site.xml
echo "  <property>" >> $HH/conf/hdfs-site.xml
echo "    <name>dfs.name.dir</name>" >> $HH/conf/hdfs-site.xml
echo "    <value>/home/hadoop/hdfs/name</value>" >> $HH/conf/hdfs-site.xml
echo "  </property>" >> $HH/conf/hdfs-site.xml
echo "" >> $HH/conf/hdfs-site.xml
echo "  <property>" >> $HH/conf/hdfs-site.xml
echo "    <name>dfs.data.dir</name>" >> $HH/conf/hdfs-site.xml
echo "    <value>/home/hadoop/hdfs/data</value>" >> $HH/conf/hdfs-site.xml
echo "  </property>" >> $HH/conf/hdfs-site.xml
echo "" >> $HH/conf/hdfs-site.xml
echo "  <property>" >> $HH/conf/hdfs-site.xml
echo "    <name>dfs.replication</name>" >> $HH/conf/hdfs-site.xml
echo "    <value>3</value>" >> $HH/conf/hdfs-site.xml
echo "  </property>" >> $HH/conf/hdfs-site.xml
echo "</configuration>" >> $HH/conf/hdfs-site.xml

# mapred-site.xml
echo "<?xml version=\"1.0\"?>" > $HH/conf/mapred-site.xml
echo "<?xml-stylesheet type=\"text/xsl\" href=\"configuration.xsl\"?>" >> $HH/conf/mapred-site.xml
echo "" >> $HH/conf/mapred-site.xml
echo "<!-- Put site-specific property overrides in this file. -->" >> $HH/conf/mapred-site.xml
echo "" >> $HH/conf/mapred-site.xml
echo "<configuration>" >> $HH/conf/mapred-site.xml
echo "  <property>" >> $HH/conf/mapred-site.xml
echo "    <name>mapred.job.tracker</name>" >> $HH/conf/mapred-site.xml
echo "    <value>master:9001</value>" >> $HH/conf/mapred-site.xml
echo "  </property>" >> $HH/conf/mapred-site.xml
echo "</configuration>" >> $HH/conf/mapred-site.xml

# masters, slaves
echo "master" > $HH/conf/masters
echo "slave1" > $HH/conf/slaves
echo "slave2" >> $HH/conf/slaves
#====#

# Environment Setting
chown -R hadoop:hadoop /home/hadoop
chmod 755 -R /home/hadoop
echo "" >> ~hadoop/.bashrc
echo "export JAVA_HOME=$JH" >> ~hadoop/.bashrc
echo "export M2_HOME=$tools/maven" >> ~hadoop/.bashrc
echo "export PATH=\$PATH:\$JAVA_HOME/bin:\$HH/bin" >> ~hadoop/.bashrc
echo "export PATH=\$PATH:\$M2_HOME/bin" >> ~hadoop/.bashrc

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

### Shell Script 작성2
나중에 SSH의 Public Key를 전달하기 위한 스크립트를 작성해서 project 디렉토리에 넣어둡니다.
파일명은 'ssh_setting.sh'로 합니다.
```
#!/bin/bash

# SSH's public key sharing
expect << EOF
    spawn ssh-keygen -t rsa
    expect "Enter file in which to save the key (/home/hadoop//.ssh/id_rsa):"
        send "\n"
    expect "Enter passphrase (empty for no passphrase):"
        send "\n"
    expect "Enter same passphrase again:"
        send "\n"
    expect eof
EOF

cat ~/.ssh/id_rsa.pub > ~/.ssh/authorized_keys
cat ~/.ssh/id_rsa.pub | ssh hadoop@slave1 "mkdir ~/.ssh; cat > ~/.ssh/authorized_keys"
cat ~/.ssh/id_rsa.pub | ssh hadoop@slave2 "mkdir ~/.ssh; cat > ~/.ssh/authorized_keys"
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
========== VM : SSH Port ==========
master : 2222 Port
slave1 : 2200 Port
slave2 : 2201 Port
※ 주의사항 : 해당 포트들이 사용되고 있는지 꼭! 확인하세요.
master -> slave1 -> slave2 순서로 포트가 할당이 되며 순서는 아래와 같습니다.
2222 -> 2200 -> 2201 -> 2202 -> 2203 -> 2204...
중간에 사용이 되고 있는 port가 있을 경우, 다음 번호를 사용하게 됩니다.
(즉 2222포트가 사용중이라면 master의 포트는 2200이 됩니다.)

========== 아이디 : 패스워드 ==========
root : vagant
vagrant : vagrant
hadoop : hadoop
```

### SSH public key 공유(master에서만 수행)
우선 master VM으로 위의 port를 참고하여 접속합니다. (hadoop/hadoop)
그 후에 public key를 공유하기 위한 쉘 스크립트를 수행
각 슬레이브에 대해서 'yes' -> 'hadoop' 패스워드를 입력해 주면 됩니다.
```
cd ~/
./ssh_setting.sh

========== logs ==========
hadoop@master:/home/hadoop$ ./ssh_setting.sh
spawn ssh-keygen -t rsa
Generating public/private rsa key pair.
Enter file in which to save the key (/home/hadoop//.ssh/id_rsa):
Created directory '/home/hadoop//.ssh'.
Enter passphrase (empty for no passphrase):
Enter same passphrase again:
Your identification has been saved in /home/hadoop//.ssh/id_rsa.
Your public key has been saved in /home/hadoop//.ssh/id_rsa.pub.
The key fingerprint is:
d8:dc:17:08:58:e3:9e:29:2c:58:ed:62:b4:94:81:7b hadoop@master
The key's randomart image is:
+--[ RSA 2048]----+
|   ..  o+        |
|  .  +.. o .     |
|   .= . . . .    |
|  .=E+ = +   .   |
|  ..= = S . .    |
|   . o .   .     |
|                 |
|                 |
|                 |
+-----------------+
The authenticity of host 'slave1 (192.168.200.10)' can't be established.
ECDSA key fingerprint is 92:9b:5b:12:56:98:84:00:28:4f:04:13:55:1a:62:63.
Are you sure you want to continue connecting (yes/no)? yes
Warning: Permanently added 'slave1,192.168.200.10' (ECDSA) to the list of known hosts.
hadoop@slave1's password:
The authenticity of host 'slave2 (192.168.200.11)' can't be established.
ECDSA key fingerprint is c0:28:b5:f8:c5:40:3e:b1:8d:67:94:43:b5:0a:6c:75.
Are you sure you want to continue connecting (yes/no)? yes
Warning: Permanently added 'slave2,192.168.200.11' (ECDSA) to the list of known hosts.
hadoop@slave2's password:
```

### SSH 접속이 패스워드 없이 되는지 확인
아래와 같이 패스워드가 없이 접속이 되는지 확인 후, exit로 빠져나옵니다.
```
ssh hadoop@slave1
exit
ssh hadoop@slave2
exit

========== logs ==========
hadoop@master:/home/hadoop$ ssh hadoop@slave1
Welcome to Ubuntu 14.04.1 LTS (GNU/Linux 3.13.0-44-generic x86_64)

 * Documentation:  https://help.ubuntu.com/

  System information as of Sat Jan 24 05:28:35 UTC 2015
( 생략 )
```

### 하둡 NameNode format
```
cd ~/tools/hadoop/bin
./hadoop namenode -format
위에서 묻는 장면에서 꼭 대문자 Y를 입력. (소문자 안됨)

========== logs ==========
hadoop@master:/home/hadoop/tools/hadoop/bin$ ./hadoop namenode -format
Warning: $HADOOP_HOME is deprecated.

15/01/24 06:01:04 INFO namenode.NameNode: STARTUP_MSG:
/************************************************************
STARTUP_MSG: Starting NameNode
STARTUP_MSG:   host = master/192.168.200.2
STARTUP_MSG:   args = [-format]
STARTUP_MSG:   version = 1.2.1
STARTUP_MSG:   build = https://svn.apache.org/repos/asf/hadoop/common/branches/branch-1.2 -r 1503152; compiled by 'mattf' on Mon Jul 22 15:23:09 PDT 2013
STARTUP_MSG:   java = 1.7.0_65
************************************************************/
Re-format filesystem in /home/hadoop/hdfs/name ? (Y or N) Y
15/01/24 06:01:06 INFO util.GSet: Computing capacity for map BlocksMap
15/01/24 06:01:06 INFO util.GSet: VM type       = 64-bit
15/01/24 06:01:06 INFO util.GSet: 2.0% max memory = 1013645312
15/01/24 06:01:06 INFO util.GSet: capacity      = 2^21 = 2097152 entries
15/01/24 06:01:06 INFO util.GSet: recommended=2097152, actual=2097152
15/01/24 06:01:06 INFO namenode.FSNamesystem: fsOwner=hadoop
15/01/24 06:01:06 INFO namenode.FSNamesystem: supergroup=supergroup
15/01/24 06:01:06 INFO namenode.FSNamesystem: isPermissionEnabled=true
15/01/24 06:01:06 INFO namenode.FSNamesystem: dfs.block.invalidate.limit=100
15/01/24 06:01:06 INFO namenode.FSNamesystem: isAccessTokenEnabled=false accessKeyUpdateInterval=0 min(s), accessTokenLifetime=0 min(s)
15/01/24 06:01:06 INFO namenode.FSEditLog: dfs.namenode.edits.toleration.length = 0
15/01/24 06:01:06 INFO namenode.NameNode: Caching file names occuring more than 10 times
15/01/24 06:01:06 INFO common.Storage: Image file /home/hadoop/hdfs/name/current/fsimage of size 112 bytes saved in 0 seconds.
15/01/24 06:01:07 INFO namenode.FSEditLog: closing edit log: position=4, editlog=/home/hadoop/hdfs/name/current/edits
15/01/24 06:01:07 INFO namenode.FSEditLog: close success: truncate to 4, editlog=/home/hadoop/hdfs/name/current/edits
15/01/24 06:01:07 INFO common.Storage: Storage directory /home/hadoop/hdfs/name has been successfully formatted.
15/01/24 06:01:07 INFO namenode.NameNode: SHUTDOWN_MSG:
/************************************************************
SHUTDOWN_MSG: Shutting down NameNode at master/192.168.200.2
************************************************************/
```

### 하둡 실행 및 확인
```
./start-all.sh
jps

각 slave들도 jps 명령어로 확인을 해봅니다.
명령어 확인 결과로,

master : JPS, JobTracker, NameNode, SecondaryNameNode
slave : JPS, TaskTracker, DataNode
가 올라와 있으면 정상입니다.

========== logs ==========
hadoop@master:/home/hadoop/tools/hadoop/bin$ ./start-all.sh
Warning: $HADOOP_HOME is deprecated.

starting namenode, logging to /home/hadoop/tools/hadoop/logs/hadoop-hadoop-namenode-master.out
( 중략 )
slave2:
slave2: starting tasktracker, logging to /home/hadoop/tools/hadoop/logs/hadoop-hadoop-tasktracker-slave2.out
slave1: Warning: $HADOOP_HOME is deprecated.
slave1:
slave1: starting tasktracker, logging to /home/hadoop/tools/hadoop/logs/hadoop-hadoop-tasktracker-slave1.out

hadoop@master:/home/hadoop/tools/hadoop/bin$ jps
13921 Jps
13847 JobTracker
13567 NameNode
13775 SecondaryNameNode

hadoop@slave1:~$ jps
13339 Jps
13241 TaskTracker
13104 DataNode
```

### Wordcount (example jar 파일사용) Test
하둡이 제대로 실행되는지 확인하기 위해 Wordcount를 한 번 돌려보겠습니다.
```
cd ~/tools/hadoop/bin/
./hadoop dfs -mkdir input
./hadoop dfs -ls
./hadoop dfs -put ../LICENSE.txt input
./hadoop jar ../hadoop-examples-1.2.1.jar wordcount input output
./hadoop dfs -ls output
./hadoop dfs -cat output/part-r-00000

========== logs ==========
hadoop@master:/home/hadoop/tools/hadoop/bin$ ./hadoop dfs -mkdir input
hadoop@master:/home/hadoop/tools/hadoop/bin$ ./hadoop dfs -ls

Found 1 items
drwxr-xr-x   - hadoop supergroup          0 2015-01-24 06:10 /user/hadoop/input
hadoop@master:/home/hadoop/tools/hadoop/bin$ ./hadoop dfs -put ../LICENSE.txt input

hadoop@master:/home/hadoop/tools/hadoop/bin$ ./hadoop jar ../hadoop-examples-1.2.1.jar wordcount input output

15/01/24 06:10:55 INFO input.FileInputFormat: Total input paths to process : 1
15/01/24 06:10:55 INFO util.NativeCodeLoader: Loaded the native-hadoop library
15/01/24 06:10:55 WARN snappy.LoadSnappy: Snappy native library not loaded
15/01/24 06:10:55 INFO mapred.JobClient: Running job: job_201501240603_0001
15/01/24 06:10:56 INFO mapred.JobClient:  map 0% reduce 0%
15/01/24 06:11:04 INFO mapred.JobClient:  map 100% reduce 0%
15/01/24 06:11:12 INFO mapred.JobClient:  map 100% reduce 33%
15/01/24 06:11:13 INFO mapred.JobClient:  map 100% reduce 100%
15/01/24 06:11:15 INFO mapred.JobClient: Job complete: job_201501240603_0001
15/01/24 06:11:15 INFO mapred.JobClient: Counters: 29
( 중략 )
15/01/24 06:11:15 INFO mapred.JobClient:     Virtual memory (bytes) snapshot=1500114944
15/01/24 06:11:15 INFO mapred.JobClient:     Map output records=1887

hadoop@master:/home/hadoop/tools/hadoop/bin$ ./hadoop dfs -ls output

Found 3 items
-rw-r--r--   3 hadoop supergroup          0 2015-01-24 06:11 /user/hadoop/output/_SUCCESS
drwxr-xr-x   - hadoop supergroup          0 2015-01-24 06:10 /user/hadoop/output/_logs
-rw-r--r--   3 hadoop supergroup       7376 2015-01-24 06:11 /user/hadoop/output/part-r-00000

hadoop@master:/home/hadoop/tools/hadoop/bin$ ./hadoop dfs -cat output/part-r-00000

"AS     3
"Contribution"  1
"Contributor"   1
( 생략 )
```