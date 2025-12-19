package com.fusion.adapter.exception

class MissingStableIdException(clazz: Class<*>) : RuntimeException(
    "Fusion: [StableId Missing] The type '${clazz.simpleName}' does not have a stableId defined.\n" +
            "Since you enabled 'setDefaultStableIds(true)', you MUST define a stableId for every registered type.\n" +
            "Example:\n" +
            "register<${clazz.simpleName}, ...> {\n" +
            "    stableId { it.id }\n" +
            "}"
)