package com.pearson.bitesize.deployer.jobdsl

import hudson.Extension
import javaposse.jobdsl.dsl.helpers.step.StepContext
import javaposse.jobdsl.plugin.DslExtensionMethod
import javaposse.jobdsl.plugin.ContextExtensionPoint

import com.pearson.bitesize.deployer.builder.EnvironmentOperatorBuilder

@Extension(optional=true)
public class EnvironmentOperatorDeployerExtension extends ContextExtensionPoint {

  @DslExtensionMethod(context=StepContext.class)
  public Object deploy_to_environment(Runnable closure) {
    def ctx = new EnvironmentOperatorDeployerContext()
    executeInContext(closure, ctx)
    return new EnvironmentOperatorBuilder(
      ctx.endpoint,
      ctx.token,
      ctx.service,
      ctx.application,
      ctx.version
    )
  }

}
