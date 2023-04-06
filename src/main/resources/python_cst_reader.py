import inspect
import json
import pickle
import sys
from typing import Optional, Tuple, List, Sequence, Union

import libcst
import libcst as cst
from libcst import *
from py4j.java_gateway import JavaGateway

# no visiting, but dumped entirely
dumpNodes = (EmptyLine, BaseExpression)

# no visiting, no dumping, saved with parent dump
# these nodes need to be kept with their parent as identifier (i.e. Name)
skipNodes = (ImportAlias, AssignTarget, ImportFrom, NameItem, WithItem, MaybeSentinel, ExceptHandler, ExceptStarHandler, Finally)


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
        self.parentFieldStack = []
        self.visitReq = [False]

    def on_visit(self, node: CSTNode) -> bool:

        if isinstance(node, BaseSuite):
            # can not remove, but further visiting needed (keep node with parent, add attributes with parent-field info)
            return True

        if self.visitReq[-1]:
            # skip node, it can not be removed
            return False

        if isinstance(node, skipNodes):
            return False

        if isinstance(node, dumpNodes):
            self.stack.append(self.currentNode)
            self.currentNode = self.currentNode.addTypeNode()
            return False

        self.stack.append(self.currentNode)
        self.currentNode = self.currentNode.addTypeNode()
        return True

    def on_leave(self, original_node: CSTNodeT, updated_node: CSTNodeT) -> Union[
        CSTNodeT, RemovalSentinel, FlattenSentinel[CSTNodeT]]:

        if isinstance(original_node, BaseSuite):
            # can not remove, but further visiting needed (keep node with parent, add attributes with parent-field info)
            return updated_node

        if self.visitReq[-1]:
            # skip node, it can not be removed
            return updated_node

        if isinstance(original_node, skipNodes):
            return updated_node

        if isinstance(original_node, dumpNodes):
            if isinstance(original_node, EmptyLine):
                if updated_node.comment is None:
                    updated_node = updated_node.with_changes(indent=True)

            self.currentNode.setBytes(pickle.dumps(updated_node))
            self.currentNode = self.stack.pop()
            return RemovalSentinel.REMOVE

        self.currentNode.setBytes(pickle.dumps(updated_node))
        self.currentNode = self.stack.pop()
        return RemovalSentinel.REMOVE

    def on_visit_attribute(self, node: CSTNode, attribute: str) -> None:
        if getattr(node, attribute) is None:  # ignore empty attributes
            return

        if isinstance(getattr(node, attribute), (list, tuple)) and len(getattr(node, attribute)) is 0:
            return

        if isinstance(getattr(node, attribute), BaseSuite):
            self.parentFieldStack.append(attribute)
            return

        # check if attribute is removable
        if visit_required(node, attribute):
            self.visitReq.append(True)  # no attribute node needed
        else:
            self.visitReq.append(False)

            self.stack.append(self.currentNode)
            self.currentNode = self.currentNode.addFieldNode(attribute)

            if isinstance(node, BaseSuite):
                self.currentNode.setParentFieldName(self.parentFieldStack[-1])

            # lists and tuples can have elements of same type > ordered node
            if isinstance(getattr(node, attribute), (list, tuple)):
                self.currentNode.makeOrdered()

    def on_leave_attribute(self, original_node: CSTNode, attribute: str) -> None:
        if getattr(original_node, attribute) is None:  # ignore empty attributes
            return

        if isinstance(getattr(original_node, attribute), (list, tuple)) and len(getattr(original_node, attribute)) is 0:
            return

        if isinstance(getattr(original_node, attribute), BaseSuite):
            # attribute can not be removed - did not create field-node
            self.parentFieldStack.pop()
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


def parseJupyterCellLines(cell, parentNode):
    parentNode.makeOrdered()
    # metadata??
    for source_line in cell["source"]:
        parentNode.addJupyterLineNode(source_line)


def parseJson(cell, parentNode):

    if isinstance(cell, dict):
        objectNode = parentNode.addJsonObjectNode()
        for key, value in cell.items():
            fieldNode = objectNode.addJsonFieldNode(key)
            parseJson(value, fieldNode)

    elif isinstance(cell, list):
        arrayNode = parentNode.addJsonArrayNode()
        for element in cell:
            parseJson(element, arrayNode)

    elif isinstance(cell, bool):
        parentNode.addJsonBooleanNode(cell)
    elif isinstance(cell, str):
        parentNode.addJsonStringNode(cell)
    elif isinstance(cell, int):
        parentNode.addJsonIntegerNode(cell)
    elif isinstance(cell, float):
        parentNode.addJsonRealNumberNode(cell)
    elif cell is None:
        parentNode.addJsonNullValueNode()
    else:
        raise Exception("Unexpected value found in Json-Cell")


def parseJupyterCellArray(cellArray, parentNode):

    if isinstance(cellArray, list):
        arrayNode = parentNode.addJsonArrayNode()
        for cell in cellArray:
            cellNode = arrayNode.addJupyterCellNode(str(cell["cell_type"]))
            if cell["cell_type"] == "code":
                try:
                    st = ''.join(cell["source"])
                    parsedCode = parse_module(normalizeEmptyLines(st))
                    cellNode.setParseType("code")
                    parsedCode.visit(CSTReader(cellNode))
                except ParserSyntaxError:
                    cellNode.setParseType("markdown")
                    parseJupyterCellLines(cell, cellNode)
                except Exception as e:
                    print(e)

            elif cell["cell_type"] == "markdown":
                cellNode.setParseType("markdown")
                parseJupyterCellLines(cell, cellNode)
                # metadata : dict {collapsed: False}
    else:
        raise Exception("Expected Jupyter Cell-Array (list), but got " + str(type(cellArray)))


def read(fileName: str):
    print(f"\nPY: Starting Script for {fileName}")

    # access java gateway and entry point
    gateway = JavaGateway()
    ep = gateway.entry_point

    f = open(fileName, "r", -1, "UTF-8")  # open file

    if fileName.endswith(".py"):
        data = normalizeEmptyLines(f.read())
        # parse code
        code = parse_module(data)
        code.visit(CSTReader(ep.getStartingNode()))
    elif fileName.endswith(".ipynb"):
        data = json.load(f)

        if isinstance(data, dict):
            objectNode = ep.getStartingNode().addJsonObjectNode()
            for key, value in data.items():
                fieldNode = objectNode.addJsonFieldNode(key)
                if key == "cells":
                    parseJupyterCellArray(value, fieldNode)
                else:
                    parseJson(value, fieldNode)
        else:
            raise Exception("Unexpected data found in Jupyter Notebook")

    elif fileName.endswith(".json"):
        data = json.load(f)
        root = ep.getStartingNode()
        parseJson(data, root)
    else:
        raise Exception("Trying to read file with unknown file extension (" + fileName + ")")

    f.close()
    print("\nPY: Finished Script")


if __name__ == '__main__':
    try:
        read(sys.argv[1])
    except Exception as e:
        print(e)
