package org.revenj.postgres.jinq

import org.revenj.postgres.jinq.transform.{LambdaAnalysis, MetamodelUtil, LambdaInfo}

case class ScalaSpecification[T](lambda: T => Boolean) extends RevenjQuery.AnalysisSpecification[T] {
  def getAnalysisLambda(index: Int): LambdaInfo = new ScalaLambdaInfo(lambda, index)

  def test(t: T): Boolean = lambda.apply(t)

  private class ScalaLambdaInfo(lambdaObject: AnyRef, lambdaIndex: Int)
    extends LambdaInfo(lambdaIndex, 0, 1) {
    Lambda = lambdaObject

    val className = lambdaObject.getClass.getName

    override def getLambdaSourceString: String = className

    override def fullyAnalyze(metamodel: MetamodelUtil,
                              alternateClassLoader: ClassLoader,
                              isObjectEqualsSafe: Boolean,
                              isCollectionContainsSafe: Boolean,
                              throwExceptionOnFailure: Boolean): LambdaAnalysis = {
      LambdaAnalysis.fullyAnalyzeClassAsLambda(
        this,
        new LambdaAnalysis.LambdaAsClassAnalysisConfig,
        numLambdaArgs,
        metamodel,
        alternateClassLoader,
        isObjectEqualsSafe,
        isCollectionContainsSafe,
        throwExceptionOnFailure)
    }
  }

}