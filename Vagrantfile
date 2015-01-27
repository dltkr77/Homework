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