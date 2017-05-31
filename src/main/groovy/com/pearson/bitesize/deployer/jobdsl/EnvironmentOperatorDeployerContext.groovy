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
    LOG.warning("service")
    service = value
  }

  void application(String value) {
    LOG.warning("application")
    application = value
  }

  void version(String value) {
    LOG.warning("version")
    version = value
  }

  void token(String value) {
    LOG.warning("token")
    token = value
  }

  void endpoint(String value) {
    LOG.warning("endpoint")
    endpoint = value
  }

}
