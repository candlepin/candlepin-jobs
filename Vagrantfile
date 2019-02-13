# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|
  config.vm.box = "centos/7"

  if Vagrant.has_plugin?("vagrant-hostmanager")
    config.hostmanager.enabled = true
    config.hostmanager.manage_host = true
    config.hostmanager.manage_guest = false
  end

  config.vm.define "jenkins" do |jenkins|
    jenkins.vm.host_name = "jenkins.example.com"
  end


  # Provision the box;  this example shows how you can run ansible locally on the vm or from the host.
  # Locally avoids having to set up the host correctly, and from the host keeps the VM smaller.
  # You should probably pick one and not do both.

  # Runs locally on provisioned vm
  config.vm.provision "ansible_local" do |ansible|
    ansible.playbook = "ansible/bootstrap.yml"
  end

  # Runs from dev box
  config.vm.provision "ansible" do |ansible|
    ansible.playbook = "ansible/vagrant.yml"
    ansible.galaxy_role_file = "ansible/requirements.yml"
    ansible.extra_vars = { ANSIBLE_STDOUT_CALLBACK: "debug" }
    ENV.each do |key, value|
      if key.start_with?('JENKINS') then
        ansible.extra_vars[key] = value
      end
    end
  end

  config.vm.synced_folder ".", "/vagrant",
      type: "rsync",
      owner: "1001",
      group: "1001"

  config.vm.provision "shell", inline: 'chown -R jenkins:jenkins /vagrant'
end
