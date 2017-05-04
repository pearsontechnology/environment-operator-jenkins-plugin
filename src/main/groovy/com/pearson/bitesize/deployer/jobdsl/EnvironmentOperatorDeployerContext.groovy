package com.pearson.bitesize.deployer.jobdsl

import javaposse.jobdsl.dsl.Context

class EnvironmentOperatorDeployerContext implements Context {
  String service
  String application
  String version
  String token
  String endpoint

  void service(String value) {
    service = value
  }

  void application(String value) {
    application = value
  }

  void version(String value) {
    version = value
  }

  void token(String value) {
    token = value
  }

  void endpoint(String value) {
    endpoint = value
  }

}
