import jenkins.model.Jenkins
import jenkins.model.*
import hudson.markup.RawHtmlMarkupFormatter

System.setProperty("hudson.model.DirectoryBrowserSupport.CSP", "")
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
