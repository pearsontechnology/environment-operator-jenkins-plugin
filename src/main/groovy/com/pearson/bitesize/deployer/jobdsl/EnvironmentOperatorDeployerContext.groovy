package com.pearson.bitesize.deployer.jobdsl

import javaposse.jobdsl.dsl.Context
import java.util.logging.Logger

class EnvironmentOperatorDeployerContext implements Context {
  String service
  String application
  String version
  String token
  String endpoint
  private static final Logger LOG = Logger.getLogger(EnvironmentOperatorDeployerContext.class.getName())

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
