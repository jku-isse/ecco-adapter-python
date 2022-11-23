from py4j.java_gateway import JavaGateway
import sys
import ast
import pickle


class RemoveLineNo(ast.NodeTransformer):
    def generic_visit(self, node: ast.AST) -> ast.AST:
        super().generic_visit(node)
        node.lineno = 0
        node.end_lineno = 0
        return node


def parse(node: ast.AST, javaParentNode):

    curr = ep.AddTypeNode(javaParentNode)

    for fieldName in node._fields:
        field = getattr(node, fieldName)
        if field is None:
            continue

        if isinstance(field, list) and len(field) > 0 and all(isinstance(el, ast.stmt) for el in field):
            setattr(node, fieldName, None)
            fieldNode = ep.AddFieldNode(fieldName, curr)
            ep.MakeOrdered(fieldNode)
            for elem in field:
                parse(elem, fieldNode)

    ep.SetTypeNodeBytes(pickle.dumps(node), curr)


print(f"\nPY: Starting Script for {0}", sys.argv[1])

# access java gateway and entry point
gateway = JavaGateway()
ep = gateway.entry_point

# open python file
f = open(sys.argv[1], "r", -1, "UTF-8")
s = f.read()
f.close()

# parse code
code = ast.parse(s)
code = RemoveLineNo().visit(code)
print(ast.dump(code))
parse(code, None)

print("\nPY: Finished Script")

