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