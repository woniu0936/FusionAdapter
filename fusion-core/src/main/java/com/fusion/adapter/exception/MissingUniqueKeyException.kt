package com.fusion.adapter.exception

class MissingUniqueKeyException(clazz: Class<*>, delegateClass: Class<*>) : RuntimeException(
    "Fusion: [UniqueKey Missing] The delegate '${delegateClass.simpleName}' for type '${clazz.simpleName}' does not have a uniqueKey defined.\n" +
            "Since you enabled 'setDefaultItemIdEnabled(true)', you MUST define a uniqueKey for every registered type.\n" +
            "Example:\n" +
            "setup<${clazz.simpleName}> {\n" +
            "    uniqueKey { it.id }\n" +
            "}"
)