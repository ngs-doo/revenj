package org.revenj.postgres.jinq;

import org.revenj.postgres.jinq.transform.MetamodelUtil;

import java.io.IOException;
import java.util.HashMap;


public class JinqMetaModel extends MetamodelUtil {

    private static final HashMap<Class<?>, String> classSources = new HashMap<>();
    private static final HashMap<String, String> stringSources = new HashMap<>();

    public void registerProperty(Class<?> clazz, String methodName, String property) throws IOException {
        try {
            addProperty(clazz.getMethod(methodName), property);
        } catch (NoSuchMethodException e) {
            throw new IOException(e);
        }
    }

    public void registerDataSource(Class<?> clazz, String dataSource) {
        classSources.put(clazz, dataSource);
        stringSources.put(clazz.getCanonicalName(), dataSource);
    }

    @Override
    public <U> String dataSourceNameFromClass(Class<U> dataSource) {
        return classSources.get(dataSource);
    }

    @Override
    public String dataSourceNameFromClassName(String className) {
        return stringSources.get(className);
    }
}
