#include "While.h"


Pulsar::While::While(Expression* condition, Block* statements, int line) {
    this->condition = condition;
    this->statements = statements;
    this->line = line;
}

std::any Pulsar::While::accept(StmtVisitor& visitor) {
    return visitor.visitWhileStatement(this);
}

Pulsar::Expression* Pulsar::While::getCondition() {
    return this->condition;
}

Pulsar::Block* Pulsar::While::getStatements() {
    return this->statements;
}

int Pulsar::While::getLine() {
    return this->line;
}
