package org.revenj.postgres.jinq.transform;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

public class MetamodelUtilFromMetamodel extends MetamodelUtil {
    final Metamodel metamodel;

    public MetamodelUtilFromMetamodel(Metamodel metamodel) {
        this.metamodel = metamodel;

        findMetamodelGetters();
        safeMethods.addAll(fieldMethods.keySet());
        safeMethods.addAll(nLinkMethods.keySet());
    }

    protected void findMetamodelGetters() {
        for (EntityType<?> entity : metamodel.getEntities()) {
            findMetamodelEntityGetters(entity);
        }
    }

    @Override
    public <U> String dataSourceNameFromClass(Class<U> entity) {
        EntityType<U> entityType = metamodel.entity(entity);
        if (entityType == null) return null;
        return entityType.getName();
    }

    /**
     * Returns the name of the entity referred to by the given class name
     *
     * @param className
     * @return if className refers to a known JPA entity, then the
     * name of the entity if returned. If not, null is returned
     */
    @Override
    public String dataSourceNameFromClassName(String className) {
        for (EntityType<?> entity : metamodel.getEntities())
            if (entity.getJavaType().getName().equals(className))
                return entity.getName();
        return null;
    }

}
