# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|
  config.vm.box = "centos/7"

  if Vagrant.has_plugin?("vagrant-hostmanager")
    config.hostmanager.enabled = true
    config.hostmanager.manage_host = true
    config.hostmanager.manage_guest = true
  end

  config.vm.define "jenkins" do |jenkins|
    jenkins.vm.host_name = "jenkins.example.com"
  end

  config.vm.provision "ansible_local" do |ansible|
    ansible.playbook = "ansible/bootstrap.yml"
  end

  config.vm.provision "ansible" do |ansible|
    ansible.playbook = "ansible/vagrant.yml"
    ansible.galaxy_role_file = "ansible/requirements.yml"
    ansible.extra_vars = {}
    ENV.each do |key, value|
      if key.start_with?('CANDLEPIN') then
        ansible.extra_vars[key] = value
      end
    end
  end
end
