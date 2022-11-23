from py4j.java_gateway import JavaGateway
import sys
import pickle

from typing import Optional, Tuple, List, Sequence, Union

import libcst as cst
from libcst import *

import inspect

# no visiting, but dumped entirely
dumpNodes = (EmptyLine, BaseExpression)
# no visiting, no dumping, saved with parent dump
skipNodes = (Name, SimpleWhitespace, Call, Comparison, SimpleString,
             Integer, ImportAlias, Parameters, Newline, TrailingWhitespace,
             IndentedBlock, AssignTarget, Attribute, UnaryOperation, IfExp, Tuple)

skipNodes2 = (ImportAlias, AssignTarget, ImportFrom, WithItem, MaybeSentinel)


def visit_required(node, attribute):
    codeString = inspect.getsource(node._visit_and_replace_children)
    for line in codeString.splitlines():
        if line.__contains__(attribute):
            return line.__contains__(attribute + "=visit_required")
    return False


class CSTReader(CSTTransformer):
    def __init__(self):
        super().__init__()
        self.currentField = None
        self.currentNode = None

        self.stack = []
        self.visitReq = [False]

    def on_visit(self, node: CSTNode) -> bool:

        if self.visitReq[-1]:  # skip node, it can not be removed
            #print(self.visitReq[-1])
            return False

        if isinstance(node, skipNodes2):
            return False

        if isinstance(node, dumpNodes):
            self.stack.append(self.currentNode)
            self.currentNode = ep.AddTypeNode(self.currentNode)
            return False

        self.stack.append(self.currentNode)
        self.currentNode = ep.AddTypeNode(self.currentNode)

        return True

    def on_leave(self, original_node: CSTNodeT, updated_node: CSTNodeT) -> Union[
        CSTNodeT, RemovalSentinel, FlattenSentinel[CSTNodeT]]:

        if self.visitReq[-1]:  # skip node, it can not be removed
            return updated_node

        if isinstance(original_node, skipNodes2):
            return updated_node

        if isinstance(original_node, dumpNodes):
            ep.SetTypeNodeBytes(pickle.dumps(updated_node), self.currentNode)
            self.currentNode = self.stack.pop()
            return RemovalSentinel.REMOVE

        ep.SetTypeNodeBytes(pickle.dumps(updated_node), self.currentNode)
        self.currentNode = self.stack.pop()
        return RemovalSentinel.REMOVE

    def on_visit_attribute(self, node: CSTNode, attribute: str) -> None:
        if getattr(node, attribute) is None:  # ignore empty attributes
            return

        # check if attribute is removable
        if visit_required(node, attribute):
            self.visitReq.append(True)
        else:
            self.visitReq.append(False)

        self.stack.append(self.currentNode)
        self.currentNode = ep.AddFieldNode(attribute, self.currentNode)

        # lists and tuples can have elements of same type > ordered node
        if isinstance(getattr(node, attribute), (list, tuple)):
            ep.MakeOrdered(self.currentNode)

    def on_leave_attribute(self, original_node: CSTNode, attribute: str) -> None:
        if getattr(original_node, attribute) is None:  # ignore empty attributes
            return

        self.visitReq.pop()
        self.currentNode = self.stack.pop()


print(f"\nPY: Starting Script for {0}", sys.argv[1])

# access java gateway and entry point
gateway = JavaGateway()
ep = gateway.entry_point

# open python file
f = open(sys.argv[1], "r", -1, "UTF-8")
s = f.read()
f.close()

# parse code
code = parse_module(s)
reader = CSTReader()
code = code.visit(reader)

print("\nPY: Finished Script")
