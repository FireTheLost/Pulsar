package org.codepulsar.primitives.types

import org.codepulsar.primitives.Primitive
import org.codepulsar.primitives.PrimitiveType

class PDouble(var value: Double) : Primitive() {
    override fun getPrimitiveType(): PrimitiveType {
        return PrimitiveType.PR_DOUBLE
    }

    override fun isPrimitiveType(primitiveType: PrimitiveType): Boolean {
        return primitiveType == PrimitiveType.PR_DOUBLE
    }

    override fun getPrimitiveValue(): Any {
        return this.value
    }

    override fun negate(): Primitive {
        return PDouble(-this.value)
    }

    override fun not(): Primitive {
        return PNone()
    }

    override fun plus(primitive: Primitive): Primitive {
        return PDouble(this.value + (primitive as PDouble).value)
    }

    override fun minus(primitive: Primitive): Primitive {
        return PDouble(this.value - (primitive as PDouble).value)
    }

    override fun times(primitive: Primitive): Primitive {
        return PDouble(this.value * (primitive as PDouble).value)
    }

    override fun div(primitive: Primitive): Primitive {
        return PDouble(this.value / (primitive as PDouble).value)
    }

    override fun rem(primitive: Primitive): Primitive {
        return PDouble(this.value % (primitive as PDouble).value)
    }

    override fun compareGreater(primitive: Primitive): Primitive {
        return PBoolean(this.value > (primitive as PDouble).value)
    }

    override fun compareLesser(primitive: Primitive): Primitive {
        return PBoolean(this.value < (primitive as PDouble).value)
    }

    override fun toString(): String {
        return value.toString()
    }
}