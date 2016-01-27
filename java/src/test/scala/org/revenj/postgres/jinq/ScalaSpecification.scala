package org.revenj.postgres.jinq

import org.revenj.patterns.Specification
import org.revenj.postgres.jinq.transform.{LambdaAnalysis, MetamodelUtil, LambdaInfo}

case class ScalaSpecification[T](lambda: T => Boolean) extends RevenjQuery.CustomAnalysis with Specification[T] {

  def getAnalysisLambda(index: Int): LambdaInfo = {
    lambda match {
      case _: Function1[_, _] => new ScalaLambdaInfo(lambda, index, 0, 1)
      case _: Function2[_, _, _] => new ScalaLambdaInfo(lambda, index, 0, 2)
      case _ => LambdaInfo.analyze(lambda, index, true)
    }
  }

  def test(t: T): Boolean = lambda.apply(t)

  private class ScalaLambdaInfo(lambdaObject: AnyRef, lambdaIndex: Int, capturedArgs: Int, lambdaArgs: Int)
    extends LambdaInfo(lambdaIndex, capturedArgs, lambdaArgs) {
    Lambda = lambdaObject

    val className = lambdaObject.getClass.getName

    override def getLambdaSourceString: String = className

    override def fullyAnalyze(metamodel: MetamodelUtil,
                              alternateClassLoader: ClassLoader,
                              isObjectEqualsSafe: Boolean,
                              isCollectionContainsSafe: Boolean,
                              isAllEqualsSafe: Boolean,
                              throwExceptionOnFailure: Boolean): LambdaAnalysis = {
      LambdaAnalysis.fullyAnalyzeClassAsLambda(
        this,
        new LambdaAnalysis.LambdaAsClassAnalysisConfig,
        numLambdaArgs,
        metamodel,
        alternateClassLoader,
        isObjectEqualsSafe,
        isAllEqualsSafe,
        isCollectionContainsSafe,
        throwExceptionOnFailure)
    }
  }

}