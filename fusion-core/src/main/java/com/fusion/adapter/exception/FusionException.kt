package com.fusion.adapter.exception

/**
 * Base class for all Fusion exceptions.
 */
open class FusionException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Thrown when a data item is submitted but no delegate is registered for its type.
 */
class UnregisteredTypeException(item: Any) : FusionException(
    "Missing Delegate: No delegate registered for type '${item.javaClass.name}'.\n" +
            "How to fix:\n" +
            "1. Check if you called 'register<${item.javaClass.simpleName}>(...)' in your setup block.\n" +
            "2. If this type is a subclass, ensure the base class is registered."
)

/**
 * Thrown when stable IDs are enabled but an item cannot provide a unique key.
 */
class MissingUniqueKeyException(clazz: Class<*>, delegateClass: Class<*>) : FusionException(
    "Missing Unique Key: The delegate '${delegateClass.simpleName}' for type '${clazz.simpleName}' did not provide a stable ID.\n" +
            "How to fix:\n" +
            "Since stable IDs are enabled, you MUST define a unique key for every registered type:\n" +
            "register<${clazz.simpleName}, ...> {\n" +
            "    stableId { it.your_unique_id }\n" +
            "}"
)

/**
 * Thrown when TypeRouter fails to select a delegate for an item.
 */
class DispatchException(clazz: Class<*>, item: Any) : FusionException(
    "Dispatch Failed: The TypeRouter for '${clazz.simpleName}' could not find a matching delegate for item: $item.\n" +
            "How to fix:\n" +
            "1. Verify your 'match { ... }' logic in the router configuration.\n" +
            "2. Ensure all possible item states are mapped to a delegate via 'map(key, delegate)'."
)
