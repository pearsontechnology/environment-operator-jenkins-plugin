package com.pearson.bitesize.deployer.jobdsl

import hudson.Extension
import hudson.model.Item

import javaposse.jobdsl.dsl.helpers.step.StepContext
import javaposse.jobdsl.plugin.DslExtensionMethod
import javaposse.jobdsl.plugin.ContextExtensionPoint
import javaposse.jobdsl.plugin.DslEnvironment

import java.util.logging.Logger

import com.pearson.bitesize.deployer.builder.EnvironmentOperatorBuilder

@Extension(optional=true)
public class EnvironmentOperatorDeployerExtension extends ContextExtensionPoint {

  private static final Logger LOG = Logger.getLogger(EnvironmentOperatorDeployerExtension.class.getName())

  @DslExtensionMethod(context=StepContext.class)
  public Object bitesizeDeploy(Runnable closure) {
    def ctx = new EnvironmentOperatorDeployerContext()
    executeInContext(closure, ctx)
    new EnvironmentOperatorBuilder(
      ctx.endpoint,
      ctx.token,
      ctx.service,
      ctx.application,
      ctx.version
    )
  }

  @Override
  public void notifyItemCreated(Item item, DslEnvironment dslEnvironment) {
    //  notifyItemUpdated(item, dslEnvironment);
  }

}
