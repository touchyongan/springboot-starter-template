package io.touchyongan.starter_template.common.specification;

import org.hibernate.HibernateException;
import org.hibernate.property.access.internal.PropertyAccessStrategyFieldImpl;
import org.hibernate.query.ResultListTransformer;
import org.hibernate.query.TupleTransformer;
import org.hibernate.transform.AliasToBeanResultTransformer;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public class GenericResultTransformer<T> implements TupleTransformer<T>, ResultListTransformer<T> {
    private final Map<Long, T> rootMap = new LinkedHashMap<>();
    private final Class<T> resultClass;

    public GenericResultTransformer(final Class<T> resultClass) {
        this.resultClass = resultClass;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T transformTuple(final Object[] tuple,
                            final String[] aliases) {
        final var subclassToAlias = new HashMap<Class<?>, List<?>>();
        final var nestedAliases = new ArrayList<String>();
        checkAndPopulateSubClassAliasAndNestedAlias(tuple, aliases, subclassToAlias, nestedAliases);

        // Repopulate field related to root object
        final var newTuple = new Object[aliases.length - nestedAliases.size()];
        final var newAliases = new String[aliases.length - nestedAliases.size()];
        var i = 0;
        var id = 0L;
        for (int j = 0; j < aliases.length; j++) {
            if (!nestedAliases.contains(aliases[j])) {
                newTuple[i] = tuple[j];
                newAliases[i] = aliases[j];
                if (Objects.equals("id", aliases[i])) {
                    id = (Long) tuple[j];
                }
                ++i;
            }
        }
        // map result to root object
        // We know all entity has id as unique
        var root = rootMap.get(id);
        if (Objects.isNull(root)) {
            final var rootTransformer = new AliasToBeanResultTransformer(resultClass);
            root = (T) rootTransformer.transformTuple(newTuple, newAliases);
            rootMap.put(id, root);
        }
        // map result to relationship class
        mapNestedClassAndSetToRoot(subclassToAlias, root);

        return root;
    }

    @SuppressWarnings("unchecked")
    private void checkAndPopulateSubClassAliasAndNestedAlias(final Object[] tuple,
                                                             final String[] aliases,
                                                             final Map<Class<?>, List<?>> subclassToAlias,
                                                             final List<String> nestedAliases) {
        try {
            for (var i = 0; i < aliases.length; i++) {
                final var alias = aliases[i];
                // We use format childName.propertyName
                if (alias.contains(".")) {
                    nestedAliases.add(alias);

                    final var sp = alias.split("\\.");
                    final var fieldName = sp[0];
                    final var aliasName = sp[1];

                    final var subclass = resultClass.getDeclaredField(fieldName).getType();
                    if (!subclassToAlias.containsKey(subclass)) {
                        // We store metedata like fieldName, fieldValue, fieldName Type
                        final var list = new ArrayList<>();
                        list.add(new ArrayList<>());
                        list.add(new ArrayList<String>());
                        list.add(fieldName);
                        list.add(resultClass.getDeclaredField(fieldName).getGenericType());
                        subclassToAlias.put(subclass, list);
                    }
                    // Example: select id, name, course.id, course.name => it will store id, name, and its value to subclass
                    ((List<Object>) subclassToAlias.get(subclass).get(0)).add(tuple[i]);
                    ((List<String>) subclassToAlias.get(subclass).get(1)).add(aliasName);
                }
            }
        } catch (final NoSuchFieldException e) {
            throw new HibernateException("Could not instantiate result class: " + resultClass.getName());
        }
    }

    @SuppressWarnings("unchecked")
    private void mapNestedClassAndSetToRoot(final Map<Class<?>, List<?>> subclassToAlias,
                                            final Object root) {
        for (final var subclass : subclassToAlias.entrySet()) {
            var isCollection = false;
            final var accessor = PropertyAccessStrategyFieldImpl.INSTANCE;
            final var access = accessor.buildPropertyAccess(resultClass,
                    (String) subclassToAlias.get(subclass.getKey()).get(2), false);

            // Instantiate the collection type
            Object collection = null;
            Class<?> subCls = null;
            Field field = null;
            try {
                field = resultClass.getDeclaredField((String) subclassToAlias.get(subclass.getKey()).get(2));
                field.setAccessible(true);
                collection = field.get(root);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            if (Set.class.isAssignableFrom(subclass.getKey())) {
                if (Objects.isNull(collection)) {
                    collection = new HashSet<>();
                }
                isCollection = true;
            } else if (List.class.isAssignableFrom(subclass.getKey())) {
                if (Objects.isNull(collection)) {
                    collection = new ArrayList<>();
                }
                isCollection = true;
            }

            if (Objects.nonNull(collection) && isCollection) {
                final var listType = (ParameterizedType) subclassToAlias.get(subclass.getKey()).get(3);
                subCls = (Class<?>) listType.getActualTypeArguments()[0];
            } else {
                subCls = subclass.getKey();
            }

            final var subclassTransformer = new AliasToBeanResultTransformer(subCls);
            final var subObject = subclassTransformer.transformTuple(
                    ((List<Object>) subclassToAlias.get(subclass.getKey()).get(0)).toArray(),
                    ((List<Object>) subclassToAlias.get(subclass.getKey()).get(1)).toArray(new String[0])
            );

            if (isCollection) {
                ((Collection) collection).add(subObject);
                Objects.requireNonNull(access.getSetter()).set(root, collection);
            } else {
                Objects.requireNonNull(access.getSetter()).set(root, subObject);
            }
        }
    }

    @Override
    public List<T> transformList(final List<T> list) {
        return new ArrayList<>(rootMap.values());
    }
}
