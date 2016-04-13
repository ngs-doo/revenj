package org.revenj.postgres.jinq

import org.revenj.patterns.{Query, DataSource}
import org.revenj.postgres.jinq.transform.{LambdaAnalysis, LambdaInfo, MetamodelUtil}

case class ScalaSort[T <: DataSource, V <: java.lang.Comparable[V]](lambda: T => V) extends RevenjQuery.CustomAnalysis with Query.Compare[T, V] {
  def getAnalysisLambda(index: Int): LambdaInfo = new ScalaLambdaInfo(lambda, index)

  def compare(item: T): V = lambda(item)

  private class ScalaLambdaInfo(lambdaObject: AnyRef, lambdaIndex: Int)
    extends LambdaInfo(lambdaIndex, 0, 1) {
    Lambda = lambdaObject

    val className = lambdaObject.getClass.getName

    override def getLambdaSourceString: String = className

    override def fullyAnalyze(metamodel: MetamodelUtil,
                              alternateClassLoader: ClassLoader,
                              isObjectEqualsSafe: Boolean,
                              isAllEqualsSafe: Boolean,
                              isCollectionContainsSafe: Boolean,
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