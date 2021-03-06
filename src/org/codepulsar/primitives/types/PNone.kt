package org.codepulsar.primitives.types

import org.codepulsar.primitives.Primitive
import org.codepulsar.primitives.PrimitiveType

/*
    Used to implement a method that isn't applicable for that class.
*/

class PNone : Primitive() {
    override fun getPrimitiveType(): PrimitiveType {
        // A Primitive Of Type PNone Cannot Be On The Stack
        return PrimitiveType.PR_ERROR
    }

    // A Primitive Of Type PNone Cannot Be On The Stack
    override fun isPrimitiveType(primitiveType: PrimitiveType): Boolean {
        return primitiveType == PrimitiveType.PR_ERROR
    }

    override fun getPrimitiveValue(): Any {
        return PNone()
    }

    override fun negate(): Primitive {
        return PNone()
    }

    override fun not(): Primitive {
        return PNone()
    }

    override fun plus(primitive: Primitive): Primitive {
        return PNone()
    }

    override fun minus(primitive: Primitive): Primitive {
        return PNone()
    }

    override fun times(primitive: Primitive): Primitive {
        return PNone()
    }

    override fun div(primitive: Primitive): Primitive {
        return PNone()
    }

    override fun rem(primitive: Primitive): Primitive {
        return PNone()
    }

    override fun compareGreater(primitive: Primitive): Primitive {
        return PNone()
    }

    override fun compareLesser(primitive: Primitive): Primitive {
        return PNone()
    }
}