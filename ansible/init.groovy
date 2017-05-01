import jenkins.model.Jenkins
import jenkins.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*
import hudson.plugins.sshslaves.*;
import hudson.markup.RawHtmlMarkupFormatter

System.setProperty("hudson.model.DirectoryBrowserSupport.CSP", "")
System.setProperty("hudson.plugins.ircbot", "SEVERE")
System.setProperty("org.pircbotx.InputParser", "OFF")
System.setProperty("hudson.model.ParametersAction.keepUndefinedParameters", "true")
System.setProperty("jsse.enableSNIExtension", "false")
System.setProperty("file.encoding", "UTF-8")

Jenkins.instance.setMarkupFormatter(new RawHtmlMarkupFormatter(false))
Jenkins.instance.save()

Jenkins.instance.getExtensionList(
    javaposse.jobdsl.plugin.GlobalJobDslSecurityConfiguration.class
  ).each {
    it.useScriptSecurity = false
    it.save()
  }

global_domain = Domain.global()
credentials_store =
  Jenkins.instance.getExtensionList(
      'com.cloudbees.plugins.credentials.SystemCredentialsProvider'
    )[0].getStore()
credentials = new BasicSSHUserPrivateKey(
      CredentialsScope.GLOBAL,
      'fe2c79db-3166-4e61-8996-a8e7de7fbb5c',
      "jenkins",
      new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(
        'http://auto-services.usersys.redhat.com/rhsm/keys/rhsm-qe'.toURL().text
        ),
      "somePassword",
      "rhsm-qe"
    )
credentials_store.addCredentials(global_domain, credentials)
