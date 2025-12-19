package com.fusion.adapter.exception

class UnregisteredTypeException(item: Any) : RuntimeException(
    "Fusion: Item of type '${item.javaClass.name}' is not registered! " +
            "You must register a Delegate for this type, or register 'Any::class.java' as a fallback."
)