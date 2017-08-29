package com.pearson.bitesize.deployer.builder

import hudson.AbortException
import hudson.FilePath
import hudson.Launcher
import hudson.Extension

import hudson.model.Run
import hudson.model.TaskListener
import hudson.tasks.Builder
import hudson.tasks.BuildStepDescriptor
import jenkins.tasks.SimpleBuildStep
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter

import groovyx.net.http.HTTPBuilder

import javax.annotation.Nonnull

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

import java.util.logging.Logger


// EnvironmentOperatorBuilder class generates a build step in Jenkins.
// This build step performs deploy() action against environment-operator.
// Required params:
//    environment-operator endpoint
//    environment-operator authentication token
//    deployed artifact name (corresponds to service)
//    deployed artifact version
//    deployed artifact application (corresponds to docker image name)

public class EnvironmentOperatorBuilder extends Builder  implements SimpleBuildStep {
  String endpoint
  String token
  String application
  String name
  String version

  private OutputStream log
  private static final Logger LOG = Logger.getLogger(EnvironmentOperatorBuilder.class.getName())

  @DataBoundConstructor
  EnvironmentOperatorBuilder(
          @Nonnull String endpoint,
          @Nonnull String token,
          @Nonnull String name,
          @Nonnull String application,
          @Nonnull String version) {

    this.endpoint = endpoint
    this.token = token

    this.application = application
    this.name = name
    this.version = version
  }

  @Override
  void perform(
          @Nonnull Run<?, ?> run,
          @Nonnull FilePath workspace,
          @Nonnull Launcher launcher, @Nonnull TaskListener listener) throws InterruptedException, IOException {

    def log = listener.getLogger()
    def deployVersion = resolveParameter(run, version)
    def deployApplication = resolveParameter(run, application)
    def deployName = resolveParameter(run, name)

    log.println("${deployName}: deploying ${deployApplication}:${deployVersion}")
    // curl -XPOST -d '{"name":name, ... }' ...
    def postData = [
      name: deployName,
      application: deployApplication,
      version: deployVersion
    ]

    // def success = watchDeploy(log)
    def success = false

    def r = doPost(postData, log)
    if (r && r.status == "deploying") {
      success = watchDeploy(log)
    }
    if (!success) {
      r = doGet(log, "pods")
      log.println("${r.pods}")
      throw new AbortException("Deployment failed")
    }
    else{
      r = doGet(log, "pods")
      log.println("${r.pods}")
    }
  }

  def doPost(def params, def log) {

    def retval

    def url = "${endpoint}/deploy"
    def http = new HTTPBuilder(url)
    try {
      http.request( POST, JSON ) { req ->
        body = params
        headers.'Authorization' = "Bearer ${token}"

        response.success = { resp, json ->
          retval = json
        }

        response.failure = { resp ->
          log.println "POST request failed: ${resp.status} (${resp.body})"
          return null
        }

      }
    } catch(e) {
      log.println "POST request failed: ${e.message}"
      return null
    }
    return retval
  }

  def doGet(def log, def type) {
    def retval = [ status: "red" ]

    if (type == "pods") {
      def url = "${endpoint}/status/${name}/pods"
    }
    else{
      def url = "${endpoint}/status/${name}"
    }


    def http = new HTTPBuilder(url)

    try {
      http.request(GET,JSON) { req ->
        headers.'Authorization' = "Bearer ${token}"

        response.success = { resp, json ->
          retval = json
        }
        response.failure = { resp ->
          log.println "GET request failed: ${resp.status} (${resp.body})"
          return null
        }
      }
    } catch(e) {
      log.println "error GET ${url}: ${e.message}"
    }
    return retval

  }

  def watchDeploy(def log) {
    def r = [ status: "red" ]
    def tries = 0
    def maxTries = 60 // timeout = 5 mins

    while (r && r.status != "green" && tries < maxTries) {
      r = doGet(log, "service")
      tries += 1
      if (r != null) {
        log.println "[${r.status}] Waiting for deployment to finish, ${tries} out of ${maxTries}"
      } else {
        return false
      }
      sleep(5000)
    }
    if (tries >= maxTries) {
      log.println "\nTimeout during deployment reached, deployment still unhealthy:\n"
      log.print r
      return false
    }
    true
  }

  private String  resolveParameter(Run run, String p) {
    def retval = p
    if (p && p.startsWith('$')) {
      def envVar = p.substring(1)
      retval = run.envVars.get(envVar)
    }
    retval
  }

  @DataBoundSetter
  void setEndpoint(String value) {
    this.endpoint = value
  }

  @DataBoundSetter
  void setToken(String value) {
    this.token = value
  }

  @DataBoundSetter
  void setApplication(String value) {
    this.application = value
  }

  @DataBoundSetter
  void setName(String value) {
    this.name = value
  }

  @DataBoundSetter
  void setVersion(String value) {
    this.version = value
  }

  @Extension @Symbol("bitesizeDeploy")
  static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

    public DescriptorImpl() {
      load()
    }

    @Override
    public String getDisplayName() {
      return "Deploy using environment-operator";
    }

    @Override
    public boolean isApplicable(Class type) {
      return true
    }
  }
}
