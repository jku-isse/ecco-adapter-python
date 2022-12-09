import ast
import json
import pickle
import sys

import libcst as cst
from libcst import *
from py4j.java_gateway import JavaGateway, GatewayParameters, CallbackServerParameters


def parse(java_node: object) -> CSTNode:
    artifactBytes = java_node.getTypeArtifactBytes()
    cst_current_node = pickle.loads(artifactBytes)

    # print(type(cst_current_node))
    # if isinstance(cst_current_node, EmptyLine):
    #     print(cst_current_node.indent)

    java_field_nodes = java_node.getChildren()
    for fieldIdx in range(len(java_field_nodes)):
        java_field_node = java_field_nodes[fieldIdx]
        cst_attribute_name = java_field_node.getFieldArtifactName()
        # print(cst_attribute_name)

        if java_field_node.getParentFieldName() is not None:
            cst_node_to_add_field = getattr(cst_current_node, java_field_node.getParentFieldName())
        else:
            cst_node_to_add_field = cst_current_node

        java_child_nodes = java_field_node.getChildren()
        for childIdx in range(len(java_child_nodes)):
            java_child = java_child_nodes[childIdx]
            cst_child_node = parse(java_child)
            # print("\t" + str(type(cst_child_node)))

            pars = None
            if getattr(cst_node_to_add_field, cst_attribute_name) is not None:
                # orelse in If can be None
                # lpar / rPar can be MaybeSentinel -
                if not isinstance(getattr(cst_node_to_add_field, cst_attribute_name), MaybeSentinel):
                    try:
                        pars = {
                            cst_attribute_name: (*getattr(cst_node_to_add_field, cst_attribute_name), cst_child_node)}
                    except Exception as e:
                        print(cst_attribute_name)
                        print(getattr(cst_node_to_add_field, cst_attribute_name))
                        print(e)
                else:
                    pass  # MaybeSentinel - pars stays None
            else:
                pars = {cst_attribute_name: cst_child_node}

            if pars is not None:
                cst_node_to_add_field = cst_node_to_add_field.with_changes(**pars)

        if java_field_node.getParentFieldName() is not None:
            pars = {java_field_node.getParentFieldName(): cst_node_to_add_field}
            cst_current_node = cst_current_node.with_changes(**pars)
        else:
            cst_current_node = cst_node_to_add_field

    return cst_current_node


def write(fileName: str):
    print(f"\nPY: Starting Writer Script for {sys.argv[1]}")

    gateway = JavaGateway()
    ep = gateway.entry_point

    # parse code from Java Artifact Tree
    root = ep.getRoot()
    if root.isType():
        code = parse(root).code
    else:

        json_dict = {
            "nbformat": root.getNbformat(),
            "nbformat_minor": root.getNbformat_minor(),
            "cells": []
        }

        cellsNode = root.getChildren()[0]
        cellNodes = cellsNode.getChildren()
        for cellsIdx in range(len(cellNodes)):
            java_cell_node = cellNodes[cellsIdx]

            if java_cell_node.getCellType() == "markdown":
                lines = java_cell_node.getChildren()
                source = []
                for lineIdx in range(len(lines)):
                    line_node = lines[lineIdx]
                    source.append(line_node.getLine())

                json_dict["cells"].append({
                    "cell_type": java_cell_node.getCellType(),
                    "source": source,
                    "metadata": {
                        "collapsed": False
                    }
                })

            elif java_cell_node.getCellType() == "code":
                if java_cell_node.getParseType() == "markdown":

                    lines = java_cell_node.getChildren()
                    source = []
                    for lineIdx in range(len(lines)):
                        line_node = lines[lineIdx]
                        source.append(line_node.getLine())

                    json_dict["cells"].append({
                        "cell_type": java_cell_node.getCellType(),
                        "execution_count": None,
                        "outputs": [],
                        "source": source,
                        "metadata": {
                            "collapsed": False
                        }
                    })
                elif java_cell_node.getParseType() == "code":
                    if len(java_cell_node.getChildren()) != 1:
                        print("Expected 1 module node, found :" + str(len(java_cell_node.getChildren())))
                    else:
                        source = parse(java_cell_node.getChildren()[0])

                        json_dict["cells"].append({
                            "cell_type": "code",
                            "execution_count": None,
                            "outputs": [],
                            "source": source.code.splitlines(True),
                            "metadata": {
                                "collapsed": False
                            }
                        })
                else:
                    print("Unknown parse type:" + str(java_cell_node.getParseType()))
            else:
                print("Unknown cell type:" + str(java_cell_node.getCellType()))

        code = json.dumps(json_dict)

    f = open(fileName, "w", -1, "UTF-8")  # open file
    f.write(code)
    f.close()  # close file

    print("\nPY: Finished Script")


if __name__ == '__main__':
    try:
        write(sys.argv[1])
    except Exception as e:
        print(e)
