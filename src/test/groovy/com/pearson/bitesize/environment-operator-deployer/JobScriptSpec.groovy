import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.plugin.JenkinsJobManagement
import org.junit.ClassRule
import org.jvnet.hudson.test.JenkinsRule
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import javaposse.jobdsl.dsl.GeneratedItems
import groovy.io.FileType
import hudson.model.Item
import hudson.model.View
import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.dsl.GeneratedItems
import javaposse.jobdsl.dsl.GeneratedJob
import javaposse.jobdsl.dsl.GeneratedView
import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.plugin.JenkinsJobManagement
import jenkins.model.Jenkins

class JobScriptsSpec extends Specification {
  @Shared
  @ClassRule
  JenkinsRule jenkinsRule = new JenkinsRule()

  @Shared
  private File outputDir = new File('./build/debug-xml')

  def setupSpec() {
     outputDir.deleteDir()
  }

  @Unroll
  def 'test script #file.name'(File file) {
    given:
    def jobManagement = new JenkinsJobManagement(System.out, [:], new File('.'))

    when:
    def items = new DslScriptLoader(jobManagement).runScript(file.text)
    writeItems items

    then:
    noExceptionThrown()

    where:
    file << jobFiles
  }

  static List<File> getJobFiles() {
    List<File> files = []
    new File('jobs').eachFileRecurse {
      if (it.name.endsWith('.groovy')) {
        files << it
      }
    }
    files
  }

  /**
   * Write the config.xml for each generated job and view to the build dir.
   */
  void writeItems(GeneratedItems items) {
    Jenkins jenkins = jenkinsRule.jenkins

    items.jobs.each { GeneratedJob generatedJob ->
      String jobName = generatedJob.jobName
      Item item = jenkins.getItemByFullName(jobName)
      String text = new URL(jenkins.rootUrl + item.url + 'config.xml').text
      writeFile new File(outputDir, 'jobs'), jobName, text
    }

    items.views.each { GeneratedView generatedView ->
      String viewName = generatedView.name
      View view = jenkins.getView(viewName)
      String text = new URL(jenkins.rootUrl + view.url + 'config.xml').text
      writeFile new File(outputDir, 'views'), viewName, text
    }
  }

  /**
   * Write a single XML file, creating any nested dirs.
   */
  void writeFile(File dir, String name, String xml) {
    List tokens = name.split('/')
    File folderDir = tokens[0..<-1].inject(dir) { File tokenDir, String token ->
        new File(tokenDir, token)
    }
    folderDir.mkdirs()

    File xmlFile = new File(folderDir, "${tokens[-1]}.xml")
    xmlFile.text = xml
  }

}
