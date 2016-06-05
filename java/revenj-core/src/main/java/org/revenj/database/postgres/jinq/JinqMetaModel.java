package org.revenj.database.postgres.jinq;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import org.jinq.rebased.org.objectweb.asm.Type;
import org.revenj.extensibility.Container;
import org.revenj.patterns.Query;
import org.revenj.database.postgres.QueryProvider;
import org.revenj.database.postgres.jinq.transform.MetamodelUtil;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Optional;

public final class JinqMetaModel extends MetamodelUtil {

	private final HashMap<Class<?>, String> classSources = new HashMap<>();
	private final HashMap<String, String> stringSources = new HashMap<>();
	private final HashMap<Method, Query.Compare> methodGetters = new HashMap<>();

	private JinqMetaModel() {
		safeMethods.add(new MethodSignature("java/lang/ThreadLocal", "get", "()Ljava/lang/Object;"));
	}

	public static JinqMetaModel configure(Container container) {
		Optional<JinqMetaModel> tryModel = container.tryResolve(JinqMetaModel.class);
		if (tryModel.isPresent()) {
			return tryModel.get();
		}
		org.revenj.database.postgres.jinq.JinqMetaModel metamodel = new org.revenj.database.postgres.jinq.JinqMetaModel();
		container.registerInstance(MetamodelUtil.class, metamodel, false);
		container.registerInstance(JinqMetaModel.class, metamodel, false);
		DataSource dataSource = container.resolve(DataSource.class);
		ClassLoader loader = container.resolve(ClassLoader.class);
		container.registerInstance(QueryProvider.class, new RevenjQueryProvider(metamodel, loader, dataSource), false);
		return metamodel;
	}

	public <T, V> void registerProperty(
			Class<?> clazz,
			String methodName,
			String property,
			Query.Compare<T, V> getter) throws IOException {
		try {
			Method method = clazz.getMethod(methodName);
			addProperty(method, property);
			methodGetters.put(method, getter);
		} catch (NoSuchMethodException e) {
			throw new IOException(e);
		}
	}

	public void registerStatic(Class<?> clazz, String methodName, String function) throws IOException {
		try {
			addStatic(clazz.getMethod(methodName), function);
		} catch (NoSuchMethodException e) {
			throw new IOException(e);
		}
	}

	public void registerDataSource(Class<?> clazz, String dataSource) {
		classSources.put(clazz, dataSource);
		stringSources.put(clazz.getCanonicalName(), dataSource);
		String sourceTypeName = Type.getInternalName(clazz);
		MethodSignature eqMethod = new MethodSignature(sourceTypeName, "equals", "(Ljava/lang/Object;)Z");
		comparisonMethods.put(eqMethod, TypedValue.ComparisonValue.ComparisonOp.eq);
		comparisonMethodsWithObjectEquals.put(eqMethod, TypedValue.ComparisonValue.ComparisonOp.eq);
		safeMethods.add(eqMethod);
	}

	public Query.Compare findGetter(Method method) {
		return methodGetters.get(method);
	}

	@Override
	public <U> String dataSourceNameFromClass(Class<U> dataSource) {
		return classSources.get(dataSource);
	}

	@Override
	public String dataSourceNameFromClassName(String className) {
		return stringSources.get(className);
	}

	public Iterable<Class<?>> dataSources() {
		return classSources.keySet();
	}
}
