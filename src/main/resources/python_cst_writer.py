from py4j.java_gateway import JavaGateway, GatewayParameters, CallbackServerParameters
import sys
import ast
import pickle

import libcst as cst
from libcst import *


def parse(java_node: object) -> CSTNode:

    artifactBytes = ep.getTypeArtifactBytes(java_node)
    cst_current_node = pickle.loads(artifactBytes)

    java_field_nodes = java_node.getChildren()
    for fieldIdx in range(len(java_field_nodes)):
        java_field_node = java_field_nodes[fieldIdx]
        cst_attribute_name = ep.getFieldArtifactName(java_field_node)
        java_child_nodes = java_field_node.getChildren()
        for childIdx in range(len(java_child_nodes)):
            java_child = java_child_nodes[childIdx]
            cst_child_node = parse(java_child)

            if getattr(cst_current_node, cst_attribute_name) is not None and getattr(cst_current_node, cst_attribute_name) is not MaybeSentinel:
                # orelse in If can be None
                # lpar / rPar can be MaybeSentinel
                try:
                    pars = {cst_attribute_name: (*getattr(cst_current_node, cst_attribute_name), cst_child_node)}
                except Exception as e:
                    print(cst_attribute_name)
                    print(type(cst_current_node))
                    print(e)
            else:
                pars = {cst_attribute_name: cst_child_node}
            cst_current_node = cst_current_node.with_changes(**pars)

    return cst_current_node


print(f"\nPY: Starting Writer Script for {0}", sys.argv[1])

gateway = JavaGateway()
ep = gateway.entry_point

# parse code from Java Artifact Tree
code = parse(ep.getRoot())

# open python file
f = open(sys.argv[1], "w", -1, "UTF-8")
f.write(code.code)
f.close()

print("\nPY: Finished Script")
