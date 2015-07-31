package org.revenj.postgres.jinq.transform;

public class LambdaAnalysisFactory
{
   public org.revenj.postgres.jinq.transform.LambdaInfo extractSurfaceInfo(Object lambda, int lambdaIndex, boolean throwExceptionOnFailure)
   {
      return org.revenj.postgres.jinq.transform.LambdaInfo.analyze(lambda, lambdaIndex, throwExceptionOnFailure);
   }
}
