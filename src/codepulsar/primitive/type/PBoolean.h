#ifndef CODEPULSAR_PBOOLEAN_H
#define CODEPULSAR_PBOOLEAN_H

#include "../Primitive.h"
#include "PNone.h"


namespace Pulsar {
    class PBoolean: public Primitive {
        public:
            PBoolean(bool value);

            PrimitiveType getPrimitiveType() override;
            bool isPrimitiveType(PrimitiveType primitiveType) override;
            std::any getPrimitiveValue() override;

            std::any unaryNegate() override;
            std::any unaryNot() override;

            std::any plus(std::any primitive) override;
            std::any minus(std::any primitive) override;
            std::any times(std::any primitive) override;
            std::any div(std::any primitive) override;
            std::any rem(std::any primitive) override;

            std::any compareGreater(std::any primitive) override;
            std::any compareLesser(std::any primitive) override;

            std::string toString() override;

        private:
            bool value;
    };
}


#endif
