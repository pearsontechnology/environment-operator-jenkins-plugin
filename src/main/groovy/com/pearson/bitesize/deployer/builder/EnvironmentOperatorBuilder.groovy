package com.pearson.bitesize.deployer.builder

import hudson.Launcher
import hudson.Extension

import hudson.model.BuildListener
import hudson.model.AbstractBuild

import hudson.tasks.Builder
import hudson.tasks.BuildStepDescriptor

import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter

import groovy.json.JsonOutput
import groovyx.net.http.HTTPBuilder

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*


// EnvironmentOperatorBuilder class generates a build step in Jenkins.
// This build step performs deploy() action against environment-operator.
// Required params:
//    environment-operator endpoint
//    environment-operator authentication token
//    deployed artifact name (corresponds to service)
//    deployed artifact version
//    deployed artifact application (corresponds to docker image name)

class EnvironmentOperatorBuilder extends Builder {
  String endpoint
  String token
  String application
  String name
  String version

  private OutputStream log

  @DataBoundConstructor
  EnvironmentOperatorBuilder(String endpoint, String token, String name, String application, String version) {
    this.endpoint = endpoint
    this.token = token

    this.application = application
    this.name = name
    this.version = version
    this.log = System.out
  }

  @Override
  public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
    def success = false
    this.log = listener.getLogger()
    log.println("${name}: deploying ${application}:${version}")

    // curl -XPOST -d '{"name":name, ... }' ...
    def postData = [
      name: name,
      application: application,
      version: version
    ]

    success = watchDeploy()

    def r = doPost(postData)
    if (r && r.status == "deploying") {
      success = watchDeploy()
    }

    success
  }

  def doPost(def params) {

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
      log.println "error POST to ${url}: ${e.message}"
      return null
    }
    return retval
  }

  def doGet() {
    def retval = [ status: "red" ]

    def url = "${endpoint}/status/${name}"
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

  def watchDeploy() {
    def r = [ status: "red" ]
    def tries = 0
    def maxTries = 60 // timeout = 5 mins

    while (r.status != "green" && tries < maxTries) {
      r = doGet()
      tries += 1
      log.print "."
      log.flush()
      sleep(5000)
    }
    if (tries >= maxTries) {
      log.println "\nTimeout during deployment reached, deployment still unhealthy:\n"
      log.print r
      return false
    }
    true
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
  void setVersion(String version) {
    this.version = value
  }

  @Extension
  public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

    public DescriptorImpl() {
      load();
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
