import jenkins.model.Jenkins
import jenkins.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*
import hudson.plugins.sshslaves.*;
import hudson.markup.RawHtmlMarkupFormatter

ResourceUpdater.setResources();

System.setProperty("hudson.model.DirectoryBrowserSupport.CSP", "sandbox allow-scripts; default-src 'self'; style-src 'self' 'unsafe-inline'; script-src * 'unsafe-inline';")
System.setProperty("hudson.plugins.ircbot", "SEVERE")
System.setProperty("org.pircbotx.InputParser", "OFF")
System.setProperty("hudson.model.ParametersAction.keepUndefinedParameters", "true")
System.setProperty("jsse.enableSNIExtension", "false")
System.setProperty("file.encoding", "UTF-8")
System.setProperty("permissive-script-security.enabled", "true")

Jenkins.instance.setMarkupFormatter(new RawHtmlMarkupFormatter(false))
Jenkins.instance.save()

Jenkins.instance.getExtensionList(
    javaposse.jobdsl.plugin.GlobalJobDslSecurityConfiguration.class
  ).each {
    it.useScriptSecurity = false
    it.save()
  }

global_domain = Domain.global()
