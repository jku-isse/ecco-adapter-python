from py4j.java_gateway import JavaGateway
import sys
import json
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
             AssignTarget, Attribute, UnaryOperation, IfExp, Tuple)

skipNodes2 = (ImportAlias, AssignTarget, ImportFrom, WithItem, MaybeSentinel)


def visit_required(node, attribute):
    codeString = inspect.getsource(node._visit_and_replace_children)
    for line in codeString.splitlines():
        if line.__contains__(attribute):
            return line.__contains__(attribute + "=visit_required")
    return False


class CSTReader(CSTTransformer):
    def __init__(self, parentNode):
        super().__init__()
        self.currentField = None
        self.currentNode = parentNode

        self.stack = []
        self.visitReq = [False]

    def on_visit(self, node: CSTNode) -> bool:

        # if isinstance(node, BaseSuite):
        #     return True

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

        # if isinstance(original_node, BaseSuite):
        #     return updated_node

        if self.visitReq[-1]:  # skip node, it can not be removed
            return updated_node

        if isinstance(original_node, skipNodes2):
            return updated_node

        if isinstance(original_node, dumpNodes):
            if isinstance(original_node, EmptyLine):
                if updated_node.comment is None:
                    updated_node = updated_node.with_changes(indent=True)

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
            # no attribute node needed
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

        if not self.visitReq.pop():
            self.currentNode = self.stack.pop()


def normalizeEmptyLines(content: str) -> str:
    lines = []
    ok = " \t\n"
    for line in content.split('\n'):
        if all(c in ok for c in line):
            lines.append('')
        else:
            lines.append(line)
    return '\n'.join(lines)


def parseCode(code, parentNode):
    code.visit(CSTReader(parentNode))


def parseLines(cell, parentNode):
    ep.MakeOrdered(parentNode)
    # metadata??
    for source_line in cell["source"]:
        ep.AddLineNode(parentNode, source_line)


print(f"\nPY: Starting Script for {sys.argv[1]}")

# access java gateway and entry point
gateway = JavaGateway()
ep = gateway.entry_point

fileName = sys.argv[1]
# open file
f = open(fileName, "r", -1, "UTF-8")

if fileName.endswith(".py"):
    s = normalizeEmptyLines(f.read())
    # parse code
    code = parse_module(s)
    code = code.visit(CSTReader(None))
elif fileName.endswith(".ipynb"):
    data = json.load(f)
    f.close()

    root = ep.AddJupyterArtifactNode(None, data["nbformat"], data["nbformat_minor"])

    cellsField = ep.AddFieldNode("cells", root)
    ep.MakeOrdered(cellsField)

    for cell in data["cells"]:
        cellNode = ep.AddJupyterCellNode(cellsField, str(cell["cell_type"]))  # TODO add
        if cell["cell_type"] == "code":
            try:
                st = ''.join(cell["source"])
                parsedCode = parse_module(st)
                ep.setParseType(cellNode, "code")
                parseCode(parsedCode, cellNode)
            except ParserSyntaxError:
                ep.setParseType(cellNode, "markdown")  # type = code
                parseLines(cell, cellNode)
            except Exception as e:
                print(e)

        elif cell["cell_type"] == "markdown":
            ep.setParseType(cellNode, "markdown")  # type = markdown
            parseLines(cell, cellNode)
            # metadata : dict {collapsed: False}
            pass
    pass

f.close()
print("\nPY: Finished Script")
