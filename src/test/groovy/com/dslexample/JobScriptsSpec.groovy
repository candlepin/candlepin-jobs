package com.dslexample

import groovy.io.FileType
import javaposse.jobdsl.dsl.ConfigFileType
import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.plugin.ConfigFileProviderHelper
import javaposse.jobdsl.plugin.JenkinsJobManagement
import org.jenkinsci.plugins.configfiles.custom.CustomConfig
import org.junit.ClassRule
import org.jvnet.hudson.test.JenkinsRule
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Tests that all dsl scripts in the jobs directory will compile.
 */
class JobScriptsSpec extends Specification {
    @Shared
    @ClassRule
    JenkinsRule jenkinsRule = new JenkinsRule()

    @Unroll
    void 'test script #file.name'(File file) {
        given:
        JobManagement jm = new JenkinsJobManagement(System.out, [:], new File('.'))
        def configProvider = ConfigFileProviderHelper.findConfigProvider(ConfigFileType.Custom)
        CustomConfig config = new CustomConfig('id', 'candlepinPerformanceInventory', 'comment', 'content')
        configProvider.save(config)

        when:
        new DslScriptLoader(jm).runScript(file.text)

        then:
        noExceptionThrown()

        where:
        file << jobFiles
    }

    static List<File> getJobFiles() {
        List<File> files = []
        new File('jobs').eachFileRecurse(FileType.FILES) {
            if (it.name.endsWith('.groovy')) {
                files << it
            }
        }
        files
    }
}

